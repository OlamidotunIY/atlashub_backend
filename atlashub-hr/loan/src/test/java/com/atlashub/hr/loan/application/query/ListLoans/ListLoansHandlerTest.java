package com.atlashub.hr.loan.application.query.ListLoans;

import com.atlashub.hr.loan.domain.entities.EmployeeLoan;
import com.atlashub.hr.loan.domain.repositories.EmployeeLoanRepository;
import com.atlashub.hr.loan.domain.valueobject.LoanStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListLoansHandlerTest {

    private EmployeeLoanRepository repository;
    private ListLoansHandler handler;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(EmployeeLoanRepository.class);
        handler = new ListLoansHandler(repository);
    }

    @Test
    @DisplayName("Should list loans by organization and employee and status")
    void shouldListLoansByOrgAndEmployeeAndStatus() {
        Money principal = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        Money deduction = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        EmployeeLoan loan = EmployeeLoan.create(1L, 100L, 10L, principal, deduction);

        when(repository.findByOrganizationIdAndEmployeeIdAndStatus(10L, 100L, LoanStatus.PENDING_APPROVAL))
                .thenReturn(List.of(loan));

        List<LoanResult> results = handler.execute(new ListLoansQuery(10L, 100L, LoanStatus.PENDING_APPROVAL));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(1L);
        assertThat(results.get(0).employeeId()).isEqualTo(100L);
        assertThat(results.get(0).status()).isEqualTo(LoanStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("Should return empty list when no filter params provided")
    void shouldReturnEmptyListWhenNoFilter() {
        List<LoanResult> results = handler.execute(new ListLoansQuery(null, null, null));
        assertThat(results).isEmpty();
    }
}
