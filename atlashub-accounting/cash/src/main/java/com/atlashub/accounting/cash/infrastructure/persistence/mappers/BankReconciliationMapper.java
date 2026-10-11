package com.atlashub.accounting.cash.infrastructure.persistence.mappers;

import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.domain.entities.ReconciliationItem;
import com.atlashub.accounting.cash.infrastructure.persistence.entities.BankReconciliationJpaEntity;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BankReconciliationMapper
        extends DomainMapper<BankReconciliation, BankReconciliationJpaEntity> {

    @Override
    @Mapping(target = "statementClosingBalance", expression = "java(toMoney(record.getStatementClosingBalance(), record.getCurrency()))")
    @Mapping(target = "bookBalance", expression = "java(toMoney(record.getBookBalance(), record.getCurrency()))")
    @Mapping(target = "difference", expression = "java(toMoney(record.getDifference(), record.getCurrency()))")
    @Mapping(target = "matchedItems", expression = "java(java.util.List.of())")
    @Mapping(target = "unmatchedBankItems", expression = "java(java.util.List.of())")
    @Mapping(target = "unmatchedBookItems", source = "unmatchedBookItemIds")
    BankReconciliation toDomain(BankReconciliationJpaEntity record);

    @Override
    @Mapping(target = "statementClosingBalance", source = "statementClosingBalance.amount")
    @Mapping(target = "bookBalance", source = "bookBalance.amount")
    @Mapping(target = "difference", source = "difference.amount")
    @Mapping(target = "currency", source = "statementClosingBalance.currency")
    @Mapping(target = "unmatchedBookItemIds", source = "unmatchedBookItems")
    @Mapping(target = "version", ignore = true)
    BankReconciliationJpaEntity toPersistence(BankReconciliation domain);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statementClosingBalance", source = "statementClosingBalance.amount")
    @Mapping(target = "bookBalance", source = "bookBalance.amount")
    @Mapping(target = "difference", source = "difference.amount")
    @Mapping(target = "currency", source = "statementClosingBalance.currency")
    @Mapping(target = "unmatchedBookItemIds", source = "unmatchedBookItems")
    @Mapping(target = "version", ignore = true)
    void updatePersistence(BankReconciliation domain, @MappingTarget BankReconciliationJpaEntity record);

    default BankReconciliation toDomain(BankReconciliationJpaEntity record,
                                        List<BankStatementLine> unmatchedBankItems,
                                        List<ReconciliationItem> matchedItems) {
        if (record == null) {
            return null;
        }
        CurrencyCode currency = CurrencyCode.valueOf(record.getCurrency());
        return new BankReconciliation(
                record.getId(), record.getOrganizationId(), record.getBankAccountCode(), record.getStatementDate(),
                Money.of(record.getStatementClosingBalance(), currency),
                Money.of(record.getBookBalance(), currency),
                Money.of(record.getDifference(), currency),
                matchedItems, unmatchedBankItems, record.getUnmatchedBookItemIds(), record.getStatus(),
                record.getCreatedAt(), record.getReconciledAt()
        );
    }

    default Money toMoney(java.math.BigDecimal amount, String currency) {
        return Money.of(amount, CurrencyCode.valueOf(currency));
    }
}
