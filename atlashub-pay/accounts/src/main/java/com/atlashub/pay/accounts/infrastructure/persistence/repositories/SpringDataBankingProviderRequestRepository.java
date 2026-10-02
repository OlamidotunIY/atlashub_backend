package com.atlashub.pay.accounts.infrastructure.persistence.repositories;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest.RequestStatus;
import com.atlashub.pay.accounts.infrastructure.persistence.entities.BankingProviderRequestJpa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataBankingProviderRequestRepository
        extends JpaRepository<BankingProviderRequestJpa, Long> {
    Optional<BankingProviderRequestJpa> findByRequestReference(String requestReference);
    List<BankingProviderRequestJpa> findByStatusOrderByCreatedAt(RequestStatus status, Pageable pageable);
}
