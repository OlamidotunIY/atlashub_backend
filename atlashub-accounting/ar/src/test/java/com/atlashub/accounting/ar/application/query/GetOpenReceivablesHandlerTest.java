package com.atlashub.accounting.ar.application.query;

import com.atlashub.accounting.ar.application.query.GetOpenReceivables.CustomerReceivableResult;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.GetOpenReceivablesHandler;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.GetOpenReceivablesQuery;
import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOpenReceivablesHandlerTest {

    @Mock
    private CustomerReceivableRepository receivableRepository;

    @InjectMocks
    private GetOpenReceivablesHandler handler;

    private final Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
    private final LocalDate dueDate = LocalDate.now().plusDays(20);

    @Test
    @DisplayName("Should return only open receivables (OUTSTANDING or PARTIALLY_COLLECTED)")
    void execute_shouldFilterOpenReceivablesOnly() {
        CustomerReceivable r1 = CustomerReceivable.create(101L, 1L, 201L, 301L, amount, dueDate);
        CustomerReceivable r2 = CustomerReceivable.create(102L, 1L, 202L, 302L, amount, dueDate);
        r2.recordPayment(Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN)); // PARTIALLY_COLLECTED
        CustomerReceivable r3 = CustomerReceivable.create(103L, 1L, 203L, 303L, amount, dueDate);
        r3.recordPayment(amount); // COLLECTED
        CustomerReceivable r4 = CustomerReceivable.create(104L, 1L, 204L, 304L, amount, dueDate);
        r4.writeOff("Defaulted"); // WRITTEN_OFF

        when(receivableRepository.findByOrganizationId(1L))
                .thenReturn(List.of(r1, r2, r3, r4));

        List<CustomerReceivableResult> results = handler.execute(new GetOpenReceivablesQuery(1L));

        assertThat(results).hasSize(2);
        assertThat(results.stream().map(CustomerReceivableResult::id).toList())
                .containsExactly(101L, 102L);

        verify(receivableRepository).findByOrganizationId(1L);
    }
}
