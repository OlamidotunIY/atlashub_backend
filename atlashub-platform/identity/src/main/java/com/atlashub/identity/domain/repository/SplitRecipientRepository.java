package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.SplitRecipient;

import java.util.Optional;

public interface SplitRecipientRepository {
    Long nextIdentity();
    SplitRecipient save(SplitRecipient SplitRecipient);
    Optional<SplitRecipient> findById(Long id);
    Optional<SplitRecipient> findByOrganizationIdAndBankCodeAndAccountNumber(Long OrganizationId, String bankCode, String accountNumber);
}
