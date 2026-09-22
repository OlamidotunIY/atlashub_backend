package com.atlashub.accounts.infrastructure.services;

import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataOrganizationRepository;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationQueryPortAdapter implements OrganizationQueryPort {

    private final SpringDataOrganizationRepository springDataRepo;

    public OrganizationQueryPortAdapter(SpringDataOrganizationRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<OrganizationDto> findById(Long orgId) {
        return springDataRepo.findById(orgId).map(this::toDto);
    }

    @Override
    public boolean existsById(Long orgId) {
        return springDataRepo.existsById(orgId);
    }

    @Override
    public String getBaseCurrency(Long orgId) {
        return springDataRepo.findById(orgId)
                .map(OrganizationJPA::getBaseCurrency)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    @Override
    public String getCountry(Long orgId) {
        return springDataRepo.findById(orgId)
                .map(OrganizationJPA::getCountry)
                .orElseThrow(() -> new NotFoundException("Organization not found"));
    }

    private OrganizationDto toDto(OrganizationJPA jpa) {
        return new OrganizationDto(
                jpa.getId(),
                jpa.getBusinessName(),
                jpa.getBusinessType(),
                jpa.getBusinessSize(),
                jpa.getIndustry(),
                jpa.getDescription(),
                jpa.getLogoUrl(),
                jpa.getWebsiteUrl(),
                jpa.getCountry(),
                jpa.getBaseCurrency()
        );
    }
}
