package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.infrastructure.persistence.entities.TrustedDeviceJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataTrustedDeviceRepository extends JpaRepository<TrustedDeviceJpa, Long> {
    Optional<TrustedDeviceJpa> findByUserIdAndDeviceFingerprint(Long userId, String fingerPrint);
    List<TrustedDeviceJpa> findAllByUserId(Long userId);
}
