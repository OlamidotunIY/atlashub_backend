package com.atlashub.authentication.infrastructure.persistence.repositories;

import com.atlashub.authentication.domain.entities.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOtpVerification extends JpaRepository<OtpVerification, Long> {
}
