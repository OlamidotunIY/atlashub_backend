package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.application.result.OrganizationProfileDto;
import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataOrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizationQueryServiceImpl implements OrganizationQueryService {

    private final SpringDataOrganizationRepository repository;

    public OrganizationQueryServiceImpl(SpringDataOrganizationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OrganizationProfileDto> findProfileById(Long OrganizationId) {
        return repository.findById(OrganizationId)
                .map(entity -> new OrganizationProfileDto(
                        entity.getId(),
                        entity.getBusinessName(),
                        entity.getDescription(),
                        entity.getLogoUrl(),
                        entity.getComplianceStatus().name(),
                        entity.getCreatedAt()
                ));
    }
}
