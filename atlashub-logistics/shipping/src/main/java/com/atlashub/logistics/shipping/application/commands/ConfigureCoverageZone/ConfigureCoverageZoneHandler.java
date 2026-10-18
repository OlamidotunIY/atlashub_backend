package com.atlashub.logistics.shipping.application.commands.ConfigureCoverageZone;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.domain.repositories.CoverageZoneRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConfigureCoverageZoneHandler extends Command<ConfigureCoverageZoneCommand, ConfigureCoverageZoneResult> {

    private final CoverageZoneRepository coverageZoneRepository;

    public ConfigureCoverageZoneHandler(CoverageZoneRepository coverageZoneRepository) {
        this.coverageZoneRepository = coverageZoneRepository;
    }

    @Override
    @Transactional
    public ConfigureCoverageZoneResult execute(ConfigureCoverageZoneCommand input) {
        CoverageZone zone = coverageZoneRepository.findByZoneCode(input.zoneCode())
                .orElseGet(() -> CoverageZone.create(
                        coverageZoneRepository.nextIdentity(),
                        input.state(),
                        input.city(),
                        input.zoneCode(),
                        input.internalFleetCovered(),
                        input.partner3PLCovered(),
                        input.preferred3PLCarrierCode(),
                        input.urgentDeliverySupported(),
                        input.baseStandardFee(),
                        input.perKgStandardFee(),
                        input.urgentMultiplier(),
                        input.intraStateStandardMinHours(),
                        input.intraStateStandardMaxHours(),
                        input.interStateStandardMinHours(),
                        input.interStateStandardMaxHours(),
                        input.urgentMinHours(),
                        input.urgentMaxHours()
                ));

        coverageZoneRepository.save(zone);

        return new ConfigureCoverageZoneResult(
                zone.getId(),
                zone.getZoneCode(),
                zone.getState(),
                zone.getCity(),
                zone.isInternalFleetCovered(),
                zone.isPartner3PLCovered(),
                zone.isUrgentDeliverySupported(),
                zone.isActive()
        );
    }
}
