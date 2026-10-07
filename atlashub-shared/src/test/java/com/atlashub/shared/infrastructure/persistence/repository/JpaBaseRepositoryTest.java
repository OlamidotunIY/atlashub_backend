package com.atlashub.shared.infrastructure.persistence.repository;

import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaBaseRepositoryTest {

    @Test
    void updates_the_managed_record_instead_of_persisting_a_second_record_with_the_same_id() {
        JpaRepository<TestRecord, Long> jpaRepository = mock(JpaRepository.class);
        DomainMapper<TestAggregate, TestRecord> mapper = mock(DomainMapper.class);
        TestAggregate aggregate = new TestAggregate(1L);
        TestRecord managedRecord = new TestRecord(1L);

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(managedRecord));
        when(mapper.toDomain(managedRecord)).thenReturn(aggregate);

        TestRepository repository = new TestRepository(jpaRepository, mapper);

        assertSame(aggregate, repository.save(aggregate));

        verify(mapper).updatePersistence(aggregate, managedRecord);
        verify(jpaRepository, never()).save(any());
    }

    private static final class TestRepository extends JpaBaseRepository<TestAggregate, TestRecord> {

        private TestRepository(JpaRepository<TestRecord, Long> jpaRepository,
                               DomainMapper<TestAggregate, TestRecord> mapper) {
            super(jpaRepository, mapper, mock(DomainSequenceGenerator.class), mock(DomainEventPublisher.class));
        }

        @Override
        protected String getSequenceName() {
            return "test_seq";
        }
    }

    private static final class TestAggregate extends AggregateRoot<Long> {

        private final Long id;

        private TestAggregate(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    private record TestRecord(Long id) implements BaseJpaEntity {

        @Override
        public Long getId() {
            return id;
        }
    }
}
