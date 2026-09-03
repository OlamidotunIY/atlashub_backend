package com.atlashub.catalog.adapter.out.mapper;

import com.atlashub.catalog.adapter.out.entity.HubProductJpaEntity;
import com.atlashub.catalog.domain.model.HubProduct;
import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class HubProductMapper {

    public HubProductJpaEntity toEntity(HubProduct domain) {
        if (domain == null) {
            return null;
        }

        return new HubProductJpaEntity(
                domain.getId(),
                domain.getKey().name(),
                domain.getName(),
                domain.getDescription(),
                domain.getStatus().name(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                null // version is managed by JPA
        );
    }

    public HubProduct toDomain(HubProductJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new HubProduct(
                entity.getId(),
                ProductKey.valueOf(entity.getProductKey()),
                entity.getName(),
                entity.getDescription(),
                ProductStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
