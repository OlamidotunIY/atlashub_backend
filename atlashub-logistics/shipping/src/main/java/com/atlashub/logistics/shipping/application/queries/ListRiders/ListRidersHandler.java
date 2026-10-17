package com.atlashub.logistics.shipping.application.queries.ListRiders;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.logistics.shipping.domain.repositories.DispatchRiderRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ListRidersHandler extends Query<ListRidersQuery, List<RiderResult>> {

    private final DispatchRiderRepository riderRepository;

    public ListRidersHandler(DispatchRiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiderResult> execute(ListRidersQuery input) {
        List<DispatchRider> riders = riderRepository.findByOrganizationId(input.organizationId());

        return riders.stream()
                .map(r -> new RiderResult(
                        r.getId(),
                        r.getUserId(),
                        r.getZoneCode(),
                        r.getVehicleId(),
                        r.getLicenseNumber(),
                        r.getStatus().name(),
                        r.getCurrentShipmentId()
                ))
                .toList();
    }
}
