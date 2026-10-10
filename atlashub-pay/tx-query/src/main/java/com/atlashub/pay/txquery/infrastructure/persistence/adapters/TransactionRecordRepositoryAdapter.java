package com.atlashub.pay.txquery.infrastructure.persistence.adapters;

import com.atlashub.pay.txquery.domain.entities.TransactionAccountEntry;
import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import com.atlashub.pay.txquery.domain.repositories.TransactionRecordRepository;
import com.atlashub.pay.txquery.domain.valueobject.TransactionFilter;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionAccountEntryJpa;
import com.atlashub.pay.txquery.infrastructure.persistence.entities.TransactionRecordJpa;
import com.atlashub.pay.txquery.infrastructure.persistence.mappers.TransactionAccountEntryMapper;
import com.atlashub.pay.txquery.infrastructure.persistence.mappers.TransactionRecordMapper;
import com.atlashub.pay.txquery.infrastructure.persistence.repositories.SpringDataTransactionAccountEntryRepository;
import com.atlashub.pay.txquery.infrastructure.persistence.repositories.SpringDataTransactionRecordRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class TransactionRecordRepositoryAdapter implements TransactionRecordRepository {
    private final SpringDataTransactionRecordRepository records;
    private final SpringDataTransactionAccountEntryRepository entries;
    private final TransactionRecordMapper recordMapper;
    private final TransactionAccountEntryMapper entryMapper;
    private final DomainSequenceGenerator sequences;

    public TransactionRecordRepositoryAdapter(SpringDataTransactionRecordRepository records,
                                              SpringDataTransactionAccountEntryRepository entries,
                                              TransactionRecordMapper recordMapper,
                                              TransactionAccountEntryMapper entryMapper,
                                              DomainSequenceGenerator sequences) {
        this.records = records;
        this.entries = entries;
        this.recordMapper = recordMapper;
        this.entryMapper = entryMapper;
        this.sequences = sequences;
    }

    @Override public Long nextIdentity() { return sequences.nextIdentity("pay_tx_record_seq"); }
    @Override public Long nextEntryIdentity() { return sequences.nextIdentity("pay_tx_account_entry_seq"); }

    @Override
    @Transactional
    public TransactionRecord save(TransactionRecord entity) {
        TransactionRecordJpa saved = records.save(recordMapper.toPersistence(entity));
        entries.deleteAllByTransactionRecordId(saved.getId());
        entries.saveAll(entity.getAccountEntries().stream().map(entryMapper::toPersistence).toList());
        return toDomain(saved);
    }

    @Override public Optional<TransactionRecord> findById(Long id) { return records.findById(id).map(this::toDomain); }
    @Override public void deleteById(Long id) { entries.deleteAllByTransactionRecordId(id); records.deleteById(id); }
    @Override public boolean existsById(Long id) { return records.existsById(id); }
    @Override public List<TransactionRecord> findAll() { return records.findAll().stream().map(this::toDomain).toList(); }

    @Override
    public Optional<TransactionRecord> findByOrganizationIdAndEnvironmentAndReference(
            Long organizationId, ApiEnvironment environment, String reference) {
        return records.findByOrganizationIdAndEnvironmentAndReference(organizationId, environment, reference)
                .map(this::toDomain);
    }

    @Override
    public PageResult<TransactionRecord> search(TransactionFilter filter) {
        int size = Math.max(1, Math.min(filter.size(), 100));
        Sort sort = Sort.by("asc".equalsIgnoreCase(filter.sortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));
        var page = records.findAll(specification(filter), PageRequest.of(Math.max(0, filter.page()), size, sort));
        return new PageResult<>(page.getContent().stream().map(this::toDomain).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public TransactionVolume calculateVolume(Long organizationId, ApiEnvironment environment, YearMonth month) {
        ZonedDateTime from = month.atDay(1).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime to = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC);
        return new TransactionVolume(
                records.countVolume(organizationId, environment, TransactionType.CHARGE.name(), from, to),
                records.sumVolume(organizationId, environment, TransactionType.CHARGE.name(), from, to),
                records.countVolume(organizationId, environment, TransactionType.PAYOUT.name(), from, to),
                records.sumVolume(organizationId, environment, TransactionType.PAYOUT.name(), from, to), "NGN");
    }

    private TransactionRecord toDomain(TransactionRecordJpa record) {
        List<TransactionAccountEntry> accountEntries = entries.findAllByTransactionRecordId(record.getId()).stream()
                .map(entryMapper::toDomain).toList();
        return recordMapper.toDomain(record, accountEntries);
    }

    private Specification<TransactionRecordJpa> specification(TransactionFilter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), f.organizationId()));
            predicates.add(cb.equal(root.get("environment"), f.environment()));
            equal(predicates, cb, root.get("type"), f.type() == null ? null : f.type().name());
            equal(predicates, cb, root.get("status"), f.status() == null ? null : f.status().name());
            equal(predicates, cb, root.get("channel"), upper(f.channel()));
            equal(predicates, cb, root.get("provider"), upper(f.provider()));
            equal(predicates, cb, root.get("sourceSystem"), upper(f.sourceSystem()));
            equal(predicates, cb, root.get("sourceReferenceId"), f.sourceReferenceId());
            equal(predicates, cb, root.get("partyType"), upper(f.partyType()));
            equal(predicates, cb, root.get("partyReferenceId"), f.partyReferenceId());
            equal(predicates, cb, root.get("outletId"), f.outletId());
            equal(predicates, cb, root.get("currency"), upper(f.currency()));
            equal(predicates, cb, root.get("reference"), f.reference());
            if (f.minAmount() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), f.minAmount()));
            if (f.maxAmount() != null) predicates.add(cb.lessThanOrEqualTo(root.get("amount"), f.maxAmount()));
            if (f.dateFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.dateFrom()));
            if (f.dateTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.dateTo()));
            if (f.search() != null && !f.search().isBlank()) {
                String pattern = "%" + f.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("reference")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("sourceReferenceId")), pattern)));
            }
            if (f.accountId() != null || f.direction() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var entry = subquery.from(TransactionAccountEntryJpa.class);
                List<Predicate> entryPredicates = new ArrayList<>();
                entryPredicates.add(cb.equal(entry.get("transactionRecordId"), root.get("id")));
                if (f.accountId() != null) entryPredicates.add(cb.equal(entry.get("ledgerAccountId"), f.accountId()));
                if (f.direction() != null) entryPredicates.add(cb.equal(entry.get("direction"), f.direction().name()));
                subquery.select(entry.get("transactionRecordId")).where(entryPredicates.toArray(Predicate[]::new));
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String upper(String value) { return value == null ? null : value.trim().toUpperCase(Locale.ROOT); }
    private static void equal(List<Predicate> predicates, jakarta.persistence.criteria.CriteriaBuilder cb,
                              jakarta.persistence.criteria.Path<?> path, Object value) {
        if (value != null) predicates.add(cb.equal(path, value));
    }
}
