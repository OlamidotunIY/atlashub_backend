package com.atlashub.accounting.ar.domain.entities;

import com.atlashub.accounting.ar.domain.events.CustomerReceivableCreatedEvent;
import com.atlashub.accounting.ar.domain.events.ReceivableCollectedEvent;
import com.atlashub.accounting.ar.domain.events.ReceivableWrittenOffEvent;
import com.atlashub.accounting.ar.domain.exceptions.InvalidReceivableOperationException;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableAlreadyCollectedException;
import com.atlashub.accounting.ar.domain.exceptions.ReceivableWrittenOffException;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerReceivableTest {

    private final Money totalAmount = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
    private final LocalDate dueDate = LocalDate.now().plusDays(30);

    @Test
    @DisplayName("Should create CustomerReceivable with valid attributes and emit CustomerReceivableCreatedEvent")
    void create_valid_shouldSucceed() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L,
                1L,
                200L,
                300L,
                totalAmount,
                dueDate
        );

        assertThat(receivable.getId()).isEqualTo(100L);
        assertThat(receivable.getOrganizationId()).isEqualTo(1L);
        assertThat(receivable.getCustomerId()).isEqualTo(200L);
        assertThat(receivable.getSalesOrderId()).isEqualTo(300L);
        assertThat(receivable.getAmount()).isEqualTo(totalAmount);
        assertThat(receivable.getPaidAmount()).isEqualTo(Money.zero(CurrencyCode.NGN));
        assertThat(receivable.getOutstandingBalance()).isEqualTo(totalAmount);
        assertThat(receivable.getStatus()).isEqualTo(ArStatus.OUTSTANDING);
        assertThat(receivable.getDueDate()).isEqualTo(dueDate);
        assertThat(receivable.getCreatedAt()).isNotNull();

        assertThat(receivable.peekDomainEvents()).hasSize(1);
        assertThat(receivable.peekDomainEvents().getFirst()).isInstanceOf(CustomerReceivableCreatedEvent.class);
        CustomerReceivableCreatedEvent event = (CustomerReceivableCreatedEvent) receivable.peekDomainEvents().getFirst();
        assertThat(event.payload().receivableId()).isEqualTo(100L);
        assertThat(event.payload().organizationId()).isEqualTo(1L);
        assertThat(event.payload().customerId()).isEqualTo(200L);
        assertThat(event.payload().salesOrderId()).isEqualTo(300L);
        assertThat(event.payload().amount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(event.payload().currency()).isEqualTo("NGN");
    }

    @Test
    @DisplayName("Should fail creation when required fields are missing or amount is not positive")
    void create_invalidInvariants_shouldThrow() {
        assertThatThrownBy(() -> CustomerReceivable.create(null, 1L, 200L, 300L, totalAmount, dueDate))
                .isInstanceOf(InvalidReceivableOperationException.class);

        assertThatThrownBy(() -> CustomerReceivable.create(100L, null, 200L, 300L, totalAmount, dueDate))
                .isInstanceOf(InvalidReceivableOperationException.class);

        assertThatThrownBy(() -> CustomerReceivable.create(100L, 1L, null, 300L, totalAmount, dueDate))
                .isInstanceOf(InvalidReceivableOperationException.class);

        assertThatThrownBy(() -> CustomerReceivable.create(100L, 1L, 200L, null, totalAmount, dueDate))
                .isInstanceOf(InvalidReceivableOperationException.class);

        Money zeroAmount = Money.of(BigDecimal.ZERO, CurrencyCode.NGN);
        assertThatThrownBy(() -> CustomerReceivable.create(100L, 1L, 200L, 300L, zeroAmount, dueDate))
                .isInstanceOf(InvalidReceivableOperationException.class);

        assertThatThrownBy(() -> CustomerReceivable.create(100L, 1L, 200L, 300L, totalAmount, null))
                .isInstanceOf(InvalidReceivableOperationException.class);
    }

    @Test
    @DisplayName("Should record partial payment and update status to PARTIALLY_COLLECTED")
    void recordPayment_partialPayment_shouldUpdateBalanceAndStatus() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );
        receivable.pullDomainEvents();

        Money partialPayment = Money.of(new BigDecimal("40000.00"), CurrencyCode.NGN);
        receivable.recordPayment(partialPayment);

        assertThat(receivable.getStatus()).isEqualTo(ArStatus.PARTIALLY_COLLECTED);
        assertThat(receivable.getPaidAmount()).isEqualTo(partialPayment);
        assertThat(receivable.getOutstandingBalance()).isEqualTo(Money.of(new BigDecimal("60000.00"), CurrencyCode.NGN));

        assertThat(receivable.peekDomainEvents()).hasSize(1);
        assertThat(receivable.peekDomainEvents().getFirst()).isInstanceOf(ReceivableCollectedEvent.class);
        ReceivableCollectedEvent event = (ReceivableCollectedEvent) receivable.peekDomainEvents().getFirst();
        assertThat(event.payload().amountPaid()).isEqualByComparingTo(new BigDecimal("40000.00"));
        assertThat(event.payload().remainingBalance()).isEqualByComparingTo(new BigDecimal("60000.00"));
    }

    @Test
    @DisplayName("Should record full payment and update status to COLLECTED")
    void recordPayment_fullPayment_shouldMarkCollected() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );
        receivable.pullDomainEvents();

        receivable.recordPayment(totalAmount);

        assertThat(receivable.getStatus()).isEqualTo(ArStatus.COLLECTED);
        assertThat(receivable.getPaidAmount()).isEqualTo(totalAmount);
        assertThat(receivable.getOutstandingBalance()).isEqualTo(Money.zero(CurrencyCode.NGN));
    }

    @Test
    @DisplayName("Should throw exception when payment exceeds remaining balance or is non-positive")
    void recordPayment_invalidPayment_shouldThrow() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );

        Money excessivePayment = Money.of(new BigDecimal("150000.00"), CurrencyCode.NGN);
        assertThatThrownBy(() -> receivable.recordPayment(excessivePayment))
                .isInstanceOf(InvalidReceivableOperationException.class);

        Money zeroPayment = Money.of(BigDecimal.ZERO, CurrencyCode.NGN);
        assertThatThrownBy(() -> receivable.recordPayment(zeroPayment))
                .isInstanceOf(InvalidReceivableOperationException.class);
    }

    @Test
    @DisplayName("Should throw exception when attempting to record payment on already collected receivable")
    void recordPayment_whenAlreadyCollected_shouldThrow() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );
        receivable.recordPayment(totalAmount);

        Money extraPayment = Money.of(new BigDecimal("1000.00"), CurrencyCode.NGN);
        assertThatThrownBy(() -> receivable.recordPayment(extraPayment))
                .isInstanceOf(ReceivableAlreadyCollectedException.class);
    }

    @Test
    @DisplayName("Should throw exception when recording payment on written-off receivable")
    void recordPayment_whenWrittenOff_shouldThrow() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );
        receivable.writeOff("Customer bankruptcy");

        Money payment = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        assertThatThrownBy(() -> receivable.recordPayment(payment))
                .isInstanceOf(ReceivableWrittenOffException.class);
    }

    @Test
    @DisplayName("Should write off receivable and emit ReceivableWrittenOffEvent")
    void writeOff_whenOutstanding_shouldSucceed() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );
        receivable.pullDomainEvents();

        receivable.writeOff("Bad debt write off");

        assertThat(receivable.getStatus()).isEqualTo(ArStatus.WRITTEN_OFF);
        assertThat(receivable.peekDomainEvents()).hasSize(1);
        assertThat(receivable.peekDomainEvents().getFirst()).isInstanceOf(ReceivableWrittenOffEvent.class);

        ReceivableWrittenOffEvent event = (ReceivableWrittenOffEvent) receivable.peekDomainEvents().getFirst();
        assertThat(event.payload().uncollectedAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(event.payload().reason()).isEqualTo("Bad debt write off");
    }

    @Test
    @DisplayName("Should throw exception when writing off an already collected or already written-off receivable")
    void writeOff_invalidStates_shouldThrow() {
        CustomerReceivable receivable = CustomerReceivable.create(
                100L, 1L, 200L, 300L, totalAmount, dueDate
        );
        receivable.recordPayment(totalAmount);

        assertThatThrownBy(() -> receivable.writeOff("Customer refused to pay"))
                .isInstanceOf(InvalidReceivableOperationException.class);

        CustomerReceivable writtenOffReceivable = CustomerReceivable.create(
                101L, 1L, 200L, 301L, totalAmount, dueDate
        );
        writtenOffReceivable.writeOff("Reason 1");

        assertThatThrownBy(() -> writtenOffReceivable.writeOff("Reason 2"))
                .isInstanceOf(ReceivableWrittenOffException.class);
    }
}
