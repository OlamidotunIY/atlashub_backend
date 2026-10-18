package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.CoverageZoneJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataCoverageZoneRepository extends JpaRepository<CoverageZoneJpa, Long> {

    Optional<CoverageZoneJpa> findByZoneCode(String zoneCode);

    Optional<CoverageZoneJpa> findByStateIgnoreCaseAndCityIgnoreCase(String state, String city);

    List<CoverageZoneJpa> findByActiveTrue();
}
