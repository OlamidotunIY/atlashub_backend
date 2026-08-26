package com.atlashub.accounts.adapter.out.persistence.mapper;

import com.atlashub.accounts.domain.model.AccountStatus;
import com.atlashub.accounts.domain.model.VirtualAccount;
import com.atlashub.accounts.adapter.out.persistence.entity.VirtualAccountEntity;
import com.atlashub.shared.domain.valueobject.NUBAN;
import com.atlashub.shared.money.CurrencyCode;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;

@Component
public class VirtualAccountMapper {

    public VirtualAccountEntity toEntity(VirtualAccount domain) {
        if (domain == null) return null;

        return new VirtualAccountEntity(
                domain.getId(),
                domain.getIntegration(),
                domain.getCustomerCode(),
                domain.getAccountName(),
                domain.getBankName(),
                domain.getNuban() != null ? domain.getNuban().value() : null,
                domain.getStatus().name(),
                domain.getIdempotencyKey(),
                domain.getCurrency().name(),
                0, // version
                ZonedDateTime.now(), // createdAt
                ZonedDateTime.now() // updatedAt
        );
    }

    public VirtualAccount toDomain(VirtualAccountEntity entity) {
        if (entity == null) return null;

        VirtualAccount account = new VirtualAccount(
                entity.getId(),
                entity.getIntegration(),
                entity.getCustomerCode(),
                entity.getAccountName(),
                entity.getBankName(),
                entity.getIdempotencyKey(),
                CurrencyCode.valueOf(entity.getCurrency()),
                AccountStatus.valueOf(entity.getStatus()),
                entity.getNuban() != null ? new NUBAN(entity.getNuban()) : null
        );
        return account;
    }
}
