package com.atlashub.logistics.shipping.application.commands.UpdateMerchantShippingConfig;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingConfig;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingConfigRepository;
import com.atlashub.shared.application.usecase.Command;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateMerchantShippingConfigHandler extends Command<UpdateMerchantShippingConfigCommand, Void> {

    private final MerchantShippingConfigRepository configRepository;

    public UpdateMerchantShippingConfigHandler(MerchantShippingConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    @Transactional
    public Void execute(UpdateMerchantShippingConfigCommand input) {
        ZonedDateTime now = ZonedDateTime.now();
        MerchantShippingConfig config = configRepository.findByOrganizationId(input.organizationId())
                .map(existing -> {
                    existing.update(input.urgentDeliveryEnabled(), input.defaultPickupAddress(), now);
                    return existing;
                })
                .orElseGet(() -> MerchantShippingConfig.create(
                        configRepository.nextIdentity(),
                        input.organizationId(),
                        input.urgentDeliveryEnabled(),
                        input.defaultPickupAddress()
                ));

        configRepository.save(config);
        return null;
    }
}
