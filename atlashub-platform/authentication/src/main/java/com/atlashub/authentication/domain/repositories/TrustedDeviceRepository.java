package com.atlashub.authentication.domain.repositories;

import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TrustedDeviceRepository extends Repository<TrustedDevice> {
    Optional<TrustedDevice> findByUserIdAndDeviceFingerprint(Long userId, String fingerPrint);
    List<TrustedDevice> findAllByUserId(Long userId);
}
