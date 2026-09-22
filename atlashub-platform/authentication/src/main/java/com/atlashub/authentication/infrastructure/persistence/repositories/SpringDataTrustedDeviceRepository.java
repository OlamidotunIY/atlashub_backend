package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.domain.entities.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTrustedDevice extends JpaRepository<TrustedDevice, Long> {
}
