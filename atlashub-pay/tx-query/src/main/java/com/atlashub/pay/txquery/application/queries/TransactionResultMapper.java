package com.atlashub.pay.txquery.application.queries;

import com.atlashub.pay.txquery.application.queries.TransactionResults.AccountEntryResult;
import com.atlashub.pay.txquery.application.queries.TransactionResults.TransactionResult;
import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import org.springframework.stereotype.Component;

@Component
public class TransactionResultMapper {
    public TransactionResult map(TransactionRecord record) {
        return map(record, null);
    }

    public TransactionResult map(TransactionRecord record, Long accountId) {
        String direction = accountId == null
                ? record.primaryDirection().name()
                : record.getAccountEntries().stream()
                .filter(entry -> accountId.equals(entry.getLedgerAccountId()))
                .findFirst()
                .map(entry -> entry.getDirection().name())
                .orElse(record.primaryDirection().name());
        return new TransactionResult(record.getId(),record.getType().name(),record.getStatus().name(),
                record.getAmount().amount(),record.getFee()==null?null:record.getFee().amount(),
                record.getNetAmount()==null?null:record.getNetAmount().amount(),record.getAmount().currency().name(),
                direction,record.getChannel(),record.getProvider(),record.getReference(),
                record.getSourceSystem(),record.getSourceReferenceId(),record.getPartyType(),record.getPartyReferenceId(),
                record.getOutletId(),record.getRecipientName(),record.getDescription(),record.getMetadata(),
                record.getAccountEntries().stream().map(e->new AccountEntryResult(e.getLedgerAccountId(),
                        e.getAccountType(),e.getAccountName(),e.getEntryType(),e.getDirection().name(),
                        e.getAmount().amount(),e.getAmount().currency().name())).toList(),record.getCreatedAt(),
                record.getCompletedAt());
    }
}
