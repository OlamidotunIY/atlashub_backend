package com.atlashub.pay.ledger.infrastructure.persistence.adapters;

import com.atlashub.pay.ledger.infrastructure.persistence.repositories.SpringDataBalanceSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceSnapshotRepositoryAdapterTest {

    @Test
    void declaresModuleSpecificSpringBeanName() {
        Component component = BalanceSnapshotRepositoryAdapter.class.getAnnotation(Component.class);
        Repository repository = SpringDataBalanceSnapshotRepository.class.getAnnotation(Repository.class);

        assertEquals("payLedgerBalanceSnapshotRepositoryAdapter", component.value());
        assertEquals("payLedgerSpringDataBalanceSnapshotRepository", repository.value());
    }
}
