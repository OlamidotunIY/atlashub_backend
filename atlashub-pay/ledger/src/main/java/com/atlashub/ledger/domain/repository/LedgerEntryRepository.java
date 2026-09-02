package com.atlashub.ledger.domain.repository;

import com.atlashub.ledger.domain.model.LedgerEntry;
import java.util.List;
import com.atlashub.shared.application.util.PageResult;

public interface LedgerEntryRepository {
    Long nextIdentity();
    List<LedgerEntry> findByAccountIdAndIdGreaterThan(Long accountId, Long lastEntryId);
    List<LedgerEntry> findByAccountId(Long accountId);
    PageResult<LedgerEntry> findByAccountIds(List<Long> accountIds, int page, int perPage);
}
