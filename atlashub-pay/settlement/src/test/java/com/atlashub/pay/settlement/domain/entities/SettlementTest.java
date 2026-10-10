package com.atlashub.pay.settlement.domain.entities;

import com.atlashub.pay.settlement.domain.events.ProviderSettlementReceivedEvent;
import com.atlashub.pay.settlement.domain.events.SettlementDisputedEvent;
import com.atlashub.pay.settlement.domain.exceptions.InvalidSettlementStateException;
import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.pay.settlement.domain.valueobject.SettlementStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementTest {

    private static final Long SETTLEMENT_ID = 1001L;
    private static final Long ORGANIZATION_ID = 2002L;
    private static final String PROVIDER_SETTLEMENT_ID = "SETTLE_BATCH_789";
    private static final Long ANCHOR_DEPOSIT_ACCOUNT_ID = 3003L;

    private Money money(String amount) {
        return new Money(new BigDecimal(amount), CurrencyCode.NGN);
    }

    private Money money(String amount, CurrencyCode currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    private Settlement createStandardSettlement() {
        return Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("10000.00"),
                money("9850.00"),
                money("150.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "Paystack daily settlement batch"
        );
    }

    @Test
    void create_withValidData_initializesSuccessfully() {
        Settlement settlement = createStandardSettlement();

        assertNotNull(settlement);
        assertEquals(SETTLEMENT_ID, settlement.getId());
        assertEquals(ORGANIZATION_ID, settlement.getOrganizationId());
        assertEquals(PaymentProvider.PAYSTACK, settlement.getProvider());
        assertEquals(PROVIDER_SETTLEMENT_ID, settlement.getProviderSettlementId());
        assertEquals(money("10000.00"), settlement.getGrossAmount());
        assertEquals(money("9850.00"), settlement.getNetAmount());
        assertEquals(money("150.00"), settlement.getProviderFeeAmount());
        assertEquals(ANCHOR_DEPOSIT_ACCOUNT_ID, settlement.getAnchorDepositAccountId());
        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, settlement.getStatus());
        assertTrue(settlement.isAwaitingAnchorCredit());
        assertFalse(settlement.isConfirmed());
        assertFalse(settlement.isDisputed());
        assertFalse(settlement.isReconciliationRequired());
        assertNotNull(settlement.getSettledAt());
        assertNotNull(settlement.getCreatedAt());
        assertEquals("Paystack daily settlement batch", settlement.getDescription());
    }

    @Test
    void create_withExplicitStatus_initializesWithSpecifiedStatus() {
        Settlement settlement = Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.MONIEPOINT,
                PROVIDER_SETTLEMENT_ID,
                money("5000.00"),
                money("4950.00"),
                money("50.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                SettlementStatus.PROVIDER_PENDING,
                "Moniepoint pending batch"
        );

        assertEquals(SettlementStatus.PROVIDER_PENDING, settlement.getStatus());
        assertEquals(PaymentProvider.MONIEPOINT, settlement.getProvider());
    }

    @Test
    void create_withNullMandatoryFields_throwsInvalidSettlementStateException() {
        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                null,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "test"
        ));

        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                SETTLEMENT_ID,
                null,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "test"
        ));

        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                null,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "test"
        ));

        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                "   ",
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "test"
        ));

        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                null,
                ZonedDateTime.now(),
                "test"
        ));
    }

    @Test
    void create_withNegativeAmounts_throwsInvalidSettlementStateException() {
        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("-1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "test"
        ));
    }

    @Test
    void create_withMismatchedNetGrossFeeCalculation_throwsInvalidSettlementStateException() {
        // 1000 - 100 != 850
        assertThrows(InvalidSettlementStateException.class, () -> Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("850.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                "test"
        ));
    }

    @Test
    void confirm_whenAwaitingAnchorCredit_succeedsAndRegistersEvent() {
        Settlement settlement = createStandardSettlement();

        settlement.confirm("ANCHOR_TX_REF_001");

        assertTrue(settlement.isConfirmed());
        assertEquals(SettlementStatus.CONFIRMED, settlement.getStatus());
        assertEquals("ANCHOR_TX_REF_001", settlement.getAnchorTransferReference());

        List<DomainEvent<?>> events = settlement.peekDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.getFirst() instanceof ProviderSettlementReceivedEvent);

        ProviderSettlementReceivedEvent event = (ProviderSettlementReceivedEvent) events.getFirst();
        assertEquals(SETTLEMENT_ID, event.aggregateId());
        assertEquals(ORGANIZATION_ID, event.payload().organizationId());
        assertEquals("PAYSTACK", event.payload().provider());
        assertEquals(PROVIDER_SETTLEMENT_ID, event.payload().settlementReference());
        assertEquals(money("9850.00"), event.payload().amount());
    }

    @Test
    void confirm_whenAlreadyConfirmed_isIdempotent() {
        Settlement settlement = createStandardSettlement();
        settlement.confirm("ANCHOR_TX_REF_001");

        // Second confirmation is idempotent
        settlement.confirm("ANCHOR_TX_REF_001");

        assertTrue(settlement.isConfirmed());
        assertEquals(1, settlement.peekDomainEvents().size());
    }

    @Test
    void confirm_whenNotInAwaitingAnchorCredit_throwsInvalidSettlementStateException() {
        Settlement settlement = Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                SettlementStatus.PROVIDER_PENDING,
                "test"
        );

        assertThrows(InvalidSettlementStateException.class, () -> settlement.confirm("ANCHOR_TX_REF_001"));
    }

    @Test
    void confirm_withBlankAnchorTransferReference_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();

        assertThrows(InvalidSettlementStateException.class, () -> settlement.confirm("   "));
        assertThrows(InvalidSettlementStateException.class, () -> settlement.confirm(null));
    }

    @Test
    void markProviderConfirmed_whenProviderPending_transitionsToAwaitingAnchorCredit() {
        Settlement settlement = Settlement.create(
                SETTLEMENT_ID,
                ORGANIZATION_ID,
                PaymentProvider.PAYSTACK,
                PROVIDER_SETTLEMENT_ID,
                money("1000.00"),
                money("900.00"),
                money("100.00"),
                ANCHOR_DEPOSIT_ACCOUNT_ID,
                ZonedDateTime.now(),
                SettlementStatus.PROVIDER_PENDING,
                "test"
        );

        settlement.markProviderConfirmed();

        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, settlement.getStatus());
        assertTrue(settlement.isAwaitingAnchorCredit());

        // Calling again is idempotent
        settlement.markProviderConfirmed();
        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, settlement.getStatus());
    }

    @Test
    void markProviderConfirmed_whenNotProviderPending_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();
        settlement.confirm("ANCHOR_TX_REF_001");

        assertThrows(InvalidSettlementStateException.class, settlement::markProviderConfirmed);
    }

    @Test
    void requireReconciliation_whenPending_transitionsToReconciliationRequired() {
        Settlement settlement = createStandardSettlement();

        settlement.requireReconciliation("Anchor credit missing after 3 business days");

        assertEquals(SettlementStatus.RECONCILIATION_REQUIRED, settlement.getStatus());
        assertTrue(settlement.isReconciliationRequired());
        assertTrue(settlement.getDescription().contains("Anchor credit missing"));
    }

    @Test
    void requireReconciliation_whenConfirmedOrFailed_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();
        settlement.confirm("ANCHOR_TX_REF_001");

        assertThrows(InvalidSettlementStateException.class, () ->
                settlement.requireReconciliation("Reason"));
    }

    @Test
    void dispute_whenValid_transitionsToDisputedAndRegistersEvent() {
        Settlement settlement = createStandardSettlement();

        settlement.dispute("Fee calculation disputed with Paystack");

        assertEquals(SettlementStatus.DISPUTED, settlement.getStatus());
        assertTrue(settlement.isDisputed());
        assertTrue(settlement.getDescription().contains("Fee calculation disputed"));

        List<DomainEvent<?>> events = settlement.peekDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.getFirst() instanceof SettlementDisputedEvent);

        SettlementDisputedEvent event = (SettlementDisputedEvent) events.getFirst();
        assertEquals(SETTLEMENT_ID, event.aggregateId());
        assertEquals(ORGANIZATION_ID, event.payload().organizationId());
        assertEquals(PaymentProvider.PAYSTACK, event.payload().provider());
        assertEquals(PROVIDER_SETTLEMENT_ID, event.payload().providerSettlementId());
        assertEquals(money("9850.00"), event.payload().netAmount());
        assertEquals("Fee calculation disputed with Paystack", event.payload().reason());
    }

    @Test
    void dispute_whenAlreadyDisputed_isIdempotent() {
        Settlement settlement = createStandardSettlement();
        settlement.dispute("Reason 1");
        settlement.dispute("Reason 1");

        assertEquals(SettlementStatus.DISPUTED, settlement.getStatus());
        assertEquals(1, settlement.peekDomainEvents().size());
    }

    @Test
    void dispute_whenFailed_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();
        settlement.fail("Failed batch");

        assertThrows(InvalidSettlementStateException.class, () -> settlement.dispute("Reason"));
    }

    @Test
    void dispute_withBlankReason_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();

        assertThrows(InvalidSettlementStateException.class, () -> settlement.dispute("  "));
        assertThrows(InvalidSettlementStateException.class, () -> settlement.dispute(null));
    }

    @Test
    void resolveDispute_whenDisputedWithoutAnchorReference_transitionsToAwaitingAnchorCredit() {
        Settlement settlement = createStandardSettlement();
        settlement.dispute("Investigating chargeback");

        settlement.resolveDispute();

        assertEquals(SettlementStatus.AWAITING_ANCHOR_CREDIT, settlement.getStatus());
        assertTrue(settlement.isAwaitingAnchorCredit());
    }

    @Test
    void resolveDispute_whenDisputedWithAnchorReference_transitionsToConfirmed() {
        Settlement settlement = createStandardSettlement();
        settlement.confirm("ANCHOR_TX_REF_001");
        settlement.dispute("Investigating provider audit");

        settlement.resolveDispute();

        assertEquals(SettlementStatus.CONFIRMED, settlement.getStatus());
        assertTrue(settlement.isConfirmed());
    }

    @Test
    void resolveDispute_whenNotDisputed_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();

        assertThrows(InvalidSettlementStateException.class, settlement::resolveDispute);
    }

    @Test
    void fail_whenNotConfirmed_transitionsToFailed() {
        Settlement settlement = createStandardSettlement();

        settlement.fail("Provider canceled batch");

        assertEquals(SettlementStatus.FAILED, settlement.getStatus());
        assertTrue(settlement.getDescription().contains("Provider canceled batch"));

        // Idempotent call
        settlement.fail("Provider canceled batch");
        assertEquals(SettlementStatus.FAILED, settlement.getStatus());
    }

    @Test
    void fail_whenConfirmed_throwsInvalidSettlementStateException() {
        Settlement settlement = createStandardSettlement();
        settlement.confirm("ANCHOR_TX_REF_001");

        assertThrows(InvalidSettlementStateException.class, () -> settlement.fail("Cannot fail"));
    }
}
