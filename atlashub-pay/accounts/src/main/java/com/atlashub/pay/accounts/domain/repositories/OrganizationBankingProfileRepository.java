package com.atlashub.pay.accounts.domain.repositories;

import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.shared.domain.repository.Repository;

import java.util.Optional;
import com.atlashub.shared.application.security.ApiEnvironment;

public interface OrganizationBankingProfileRepository extends Repository<OrganizationBankingProfile> {
    Optional<OrganizationBankingProfile> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment);
}
