# Multi-Currency Architecture

## The Invariant

**Money in AtlasHub is never a raw number.** Every monetary value in the system — in domain models, DTOs, database columns, API responses, and event payloads — is always a `Money` value object: an amount paired with a currency code.

This eliminates the entire class of bugs where ₦50,000 and $50,000 are treated as the same thing.

---

## The `Money` Value Object

Defined in `atlashub-shared`:

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.scale() > 4)
            throw new ValidationException(SharedErrorCode.INVALID_MONEY_SCALE,
                "Money scale exceeds 4 decimal places: " + amount);
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException(SharedErrorCode.NEGATIVE_AMOUNT,
                "Money amount cannot be negative");
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount.setScale(4, RoundingMode.HALF_UP), currency);
    }

    public static Money of(long units, Currency currency) {
        return of(BigDecimal.valueOf(units), currency);
    }

    public static Money zero(Currency currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        Money result = new Money(this.amount.subtract(other.amount), this.currency);
        if (result.amount.compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessRuleException(SharedErrorCode.INSUFFICIENT_FUNDS, "Subtraction results in negative balance");
        return result;
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor).setScale(4, RoundingMode.HALF_UP), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency))
            throw new BusinessRuleException(SharedErrorCode.CURRENCY_MISMATCH,
                "Cannot operate on different currencies: " + this.currency + " vs " + other.currency);
    }

    @Override
    public String toString() {
        return currency.getSymbol() + " " + amount.toPlainString();
    }
}
```

### Database Storage
`Money` is stored as **two separate columns**, not a JSON blob or a single decimal:

```sql
-- CORRECT
amount   DECIMAL(19, 4) NOT NULL,
currency CHAR(3)        NOT NULL      -- ISO 4217 code: NGN, KES, USD, GHS, ZAR

-- WRONG — never do this
money_json JSONB   -- does not allow DB-level range queries or aggregation
```

JPA mapping uses `@Embeddable`:
```java
@Embeddable
public class MoneyEmbeddable {
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;
}
```

---

## The `Currency` Enum

```java
public enum Currency {
    NGN("₦", "Nigerian Naira"),
    KES("KSh", "Kenyan Shilling"),
    GHS("GH₵", "Ghanaian Cedi"),
    ZAR("R", "South African Rand"),
    USD("$", "US Dollar"),
    EUR("€", "Euro"),        // future
    GBP("£", "British Pound"); // future

    private final String symbol;
    private final String displayName;

    Currency(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }

    public String getSymbol() { return symbol; }

    public static Currency fromCode(String code) {
        for (Currency c : values()) {
            if (c.name().equals(code.toUpperCase())) return c;
        }
        throw new ValidationException(SharedErrorCode.UNSUPPORTED_CURRENCY, "Unsupported currency: " + code);
    }
}
```

---

## Organization Base Currency

When an organization registers, their `baseCurrency` is derived automatically from their `country`:

```java
// In Country value object
public Currency deriveCurrency() {
    return switch (code) {
        case "NG" -> Currency.NGN;
        case "KE" -> Currency.KES;
        case "GH" -> Currency.GHS;
        case "ZA" -> Currency.ZAR;
        case "US" -> Currency.USD;
        default -> throw new BusinessRuleException(AccountsErrorCode.UNSUPPORTED_COUNTRY, code);
    };
}
```

- A Nigerian organization's wallet balances, invoices, payslips, and reports are all in NGN
- A Kenyan organization's are all in KES
- No mixing: if a Nigerian org tries to record a USD transaction without an explicit currency conversion, the `Money.assertSameCurrency()` guard throws `CURRENCY_MISMATCH`

---

## Multi-Currency Operations

### Billing (AtlasHub Platform Invoices)
AtlasHub invoices organizations in their base currency. The `catalog` module stores pricing per currency:
```java
Map<Currency, Money> prices = Map.of(
    Currency.NGN, Money.of(15_000, Currency.NGN),   // ₦15,000/month
    Currency.KES, Money.of(3_000, Currency.KES),    // KSh 3,000/month
    Currency.USD, Money.of(30, Currency.USD)         // $30/month
);
```

`GenerateSubscriptionInvoiceUseCase` reads the org's `baseCurrency` and picks the corresponding price.

### Pay (Charges and Payouts)
All charges and payouts are denominated in the org's base currency. AtlasHub does not perform currency conversion at the payment layer — that is the responsibility of the payment provider (Paystack or Anchor handle FX if needed).

### Accounting (Multi-Currency Entries)
When an org records a transaction in a foreign currency (e.g., a Nigerian company invoicing a US client in USD), the accounting module records the entry in both currencies:
```
JournalLine:
  accountId: 4001 (Accounts Receivable)
  amount: $500.00 USD
  localEquivalent: ₦750,000 NGN (at rate: 1 USD = 1,500 NGN)
  exchangeRate: 1500.00
  exchangeRateDate: 2026-09-17
```
The `localEquivalent` is the amount used for P&L and Balance Sheet reporting in the org's base currency. Future unrealized FX gains/losses can be computed by re-valuing the foreign currency balances at the current rate.

---

## Exchange Rate Management

### MVP Approach
For MVP, exchange rates are **manually configured** by AtlasHub admins. A `ExchangeRate` table stores the daily rate for each currency pair. The accounting module reads the rate at the time of the journal entry.

```sql
CREATE TABLE exchange_rates (
    id           BIGSERIAL PRIMARY KEY,
    from_currency CHAR(3) NOT NULL,
    to_currency   CHAR(3) NOT NULL,
    rate          DECIMAL(19, 8) NOT NULL,   -- e.g., 1500.00000000 (NGN per USD)
    effective_date DATE NOT NULL,
    source        TEXT,                      -- 'CBN', 'MANUAL', 'PAYSTACK'
    UNIQUE (from_currency, to_currency, effective_date)
);
```

### Future Approach
Integrate with Central Bank of Nigeria (CBN) API and Wise/Open Exchange Rates for automatic daily rate updates. The `ExchangeRatePort` interface abstracts the source:

```java
public interface ExchangeRatePort {
    BigDecimal getRate(Currency from, Currency to, LocalDate date);
}
```

---

## Currency Rules Reference

| Rule | Where Enforced |
|---|---|
| `Money` always has currency | Compact constructor validation |
| Cannot add/subtract different currencies | `Money.assertSameCurrency()` |
| Amount cannot be negative | Compact constructor |
| DB stores two columns (amount + currency) | JPA `@Embeddable` + DB schema |
| Org base currency is immutable after registration | `Organization.country` is final |
| Invoice currency = org base currency | `GenerateInvoiceUseCase` reads org currency |
| All payroll figures in org base currency | `InitiatePayrollUseCase` validates |
