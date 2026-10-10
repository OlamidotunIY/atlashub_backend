package com.atlashub.pay.txquery.infrastructure.persistence.repositories;

import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionAccountEntryJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataTransactionAccountEntryRepository extends JpaRepository<TransactionAccountEntryJpa, Long> {
    List<TransactionAccountEntryJpa> findAllByTransactionRecordId(Long transactionRecordId);
    List<TransactionAccountEntryJpa> findAllByTransactionRecordIdIn(Collection<Long> transactionRecordIds);
    void deleteAllByTransactionRecordId(Long transactionRecordId);
}
