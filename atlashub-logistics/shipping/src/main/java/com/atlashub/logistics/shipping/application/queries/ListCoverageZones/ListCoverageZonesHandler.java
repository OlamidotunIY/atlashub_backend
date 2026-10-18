package com.atlashub.logistics.shipping.application.queries.ListCoverageZones;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.logistics.shipping.domain.repositories.CoverageZoneRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListCoverageZonesHandler extends Query<ListCoverageZonesQuery, List<CoverageZoneResult>> {

    private final CoverageZoneRepository coverageZoneRepository;

    public ListCoverageZonesHandler(CoverageZoneRepository coverageZoneRepository) {
        this.coverageZoneRepository = coverageZoneRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoverageZoneResult> execute(ListCoverageZonesQuery input) {
        List<CoverageZone> zones = coverageZoneRepository.findAllActive();

        return zones.stream()
                .map(z -> new CoverageZoneResult(
                        z.getId(),
                        z.getState(),
                        z.getCity(),
                        z.getZoneCode(),
                        z.isInternalFleetCovered(),
                        z.isPartner3PLCovered(),
                        z.getPreferred3PLCarrierCode(),
                        z.isUrgentDeliverySupported(),
                        z.getBaseStandardFee(),
                        z.getPerKgStandardFee(),
                        z.getUrgentMultiplier(),
                        z.getIntraStateStandardMinHours(),
                        z.getIntraStateStandardMaxHours(),
                        z.getInterStateStandardMinHours(),
                        z.getInterStateStandardMaxHours(),
                        z.getUrgentMinHours(),
                        z.getUrgentMaxHours(),
                        z.isActive()
                ))
                .toList();
    }
}
