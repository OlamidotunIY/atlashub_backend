package com.atlashub.pay.txquery.application.services;

import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import com.atlashub.pay.txquery.domain.repositories.TransactionRecordRepository;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Map;

@Component
public class TransactionProjectionSupport {
    private final TransactionRecordRepository repository;

    public TransactionProjectionSupport(TransactionRecordRepository repository) {
        this.repository = repository;
    }

    public TransactionRecord upsert(Long organizationId, String environment, TransactionType type,
                                    TransactionStatus status, Money amount, String channel, String provider,
                                    String reference, String sourceSystem, String sourceReferenceId,
                                    String partyType, String partyReferenceId, Long outletId,
                                    String recipientName, String recipientAccountNumber, String description,
                                    Map<String, String> metadata, ZonedDateTime occurredAt) {
        ApiEnvironment apiEnvironment = ApiEnvironment.parse(environment);
        TransactionRecord record = repository.findByOrganizationIdAndEnvironmentAndReference(
                        organizationId, apiEnvironment, reference)
                .orElseGet(() -> TransactionRecord.create(repository.nextIdentity(), organizationId, apiEnvironment,
                        type, status, amount, channel, provider, reference, sourceSystem, sourceReferenceId,
                        partyType, partyReferenceId, outletId, recipientName, recipientAccountNumber,
                        description, metadata, occurredAt));
        record.enrich(type, channel, provider, sourceSystem, sourceReferenceId, partyType, partyReferenceId,
                outletId, recipientName, recipientAccountNumber);
        record.updateStatus(status, occurredAt, description);
        return repository.save(record);
    }

    public TransactionRecordRepository repository() {
        return repository;
    }
}
