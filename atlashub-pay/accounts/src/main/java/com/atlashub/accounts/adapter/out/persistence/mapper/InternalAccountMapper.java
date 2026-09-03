package com.atlashub.accounts.adapter.out.persistence.mapper;

import com.atlashub.accounts.adapter.out.persistence.entity.InternalAccountEntity;
import com.atlashub.accounts.domain.model.InternalAccount;
import com.atlashub.accounts.domain.valueobject.InternalAccountStatus;
import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.domain.money.CurrencyCode;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class InternalAccountMapper {

    public InternalAccountEntity toEntity(InternalAccount domain) {
        if (domain == null) return null;

        return new InternalAccountEntity(
                domain.getId(),
                domain.getOrganizationId(),
                domain.getType().name(),
                domain.getCurrency().name(),
                domain.getStatus().name(),
                0, // version handled by JPA
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );
    }

    public InternalAccount toDomain(InternalAccountEntity entity) {
        if (entity == null) return null;

        return new InternalAccount(
                entity.getId(),
                entity.getOrganizationId(),
                InternalAccountType.valueOf(entity.getType()),
                CurrencyCode.valueOf(entity.getCurrency()),
                InternalAccountStatus.valueOf(entity.getStatus())
        );
    }
}