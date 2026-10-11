package com.atlashub.pay.ledger.infrastructure.persistence.repositories;

import com.atlashub.pay.ledger.infrastructure.persistence.entities.LedgerAccountJpa;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface SpringDataLedgerAccountRepository extends JpaRepository<LedgerAccountJpa, Long> {

    @Override
    @EntityGraph(attributePaths = "activeRestrictions")
    Optional<LedgerAccountJpa> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "activeRestrictions")
    List<LedgerAccountJpa> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @EntityGraph(attributePaths = "activeRestrictions")
    @Query("select account from LedgerAccountJpa account where account.id = :id")
    Optional<LedgerAccountJpa> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = "activeRestrictions")
    List<LedgerAccountJpa> findAllByOrganizationId(Long organizationId);

    @EntityGraph(attributePaths = "activeRestrictions")
    List<LedgerAccountJpa> findAllByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);

    @EntityGraph(attributePaths = "activeRestrictions")
    Optional<LedgerAccountJpa> findByOrganizationIdAndEnvironmentAndAccountNameIgnoreCaseAndCurrency(
            Long organizationId, ApiEnvironment environment, String accountName, String currency);

    @EntityGraph(attributePaths = "activeRestrictions")
    Optional<LedgerAccountJpa> findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
            Long organizationId, ApiEnvironment environment, String accountType, String currency);

    @EntityGraph(attributePaths = "activeRestrictions")
    Optional<LedgerAccountJpa> findByOrganizationIdAndEnvironmentAndOutletIdAndCurrency(
            Long organizationId, ApiEnvironment environment, Long outletId, String currency);

    @EntityGraph(attributePaths = "activeRestrictions")
    Optional<LedgerAccountJpa> findByOrganizationIdAndEnvironmentAndPartyTypeAndPartyReferenceIdAndAccountTypeAndCurrency(
            Long organizationId, ApiEnvironment environment, String partyType,
            String partyReferenceId, String accountType, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @EntityGraph(attributePaths = "activeRestrictions")
    @Query("select account from LedgerAccountJpa account where account.id in :ids order by account.id")
    List<LedgerAccountJpa> findAllByIdIn(@Param("ids") List<Long> ids);
}
