package com.atlashub.shared.application.port;

import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GlAccountQueryPort {

    Optional<Money> getAccountBalance(Long organizationId, String accountCode, LocalDate asOf);

    List<Long> getJournalLineIds(Long organizationId, String accountCode, LocalDate asOf);
}
