package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.CoverageZone;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface CoverageZoneRepository extends Repository<CoverageZone> {

    Optional<CoverageZone> findByZoneCode(String zoneCode);

    Optional<CoverageZone> findByStateAndCity(String state, String city);

    List<CoverageZone> findAllActive();
}
