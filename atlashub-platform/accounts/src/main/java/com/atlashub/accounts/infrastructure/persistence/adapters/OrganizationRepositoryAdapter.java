package com.atlashub.accounts.infrastructure.persistence.adapters;

import com.atlashub.accounts.domain.model.Organization;
import com.atlashub.accounts.domain.repository.OrganizationRepository;
import com.atlashub.accounts.infrastructure.persistence.entities.OrganizationJPA;
import com.atlashub.accounts.infrastructure.persistence.mappers.OrganizationMapper;
import com.atlashub.accounts.infrastructure.persistence.repositories.SpringDataOrganizationRepository;
import com.atlashub.accounts.infrastructure.services.OrganizationQueryPortAdapter;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OrganizationRepositoryAdapter extends JpaBaseRepository<Organization, OrganizationJPA> implements OrganizationRepository {

    private final SpringDataOrganizationRepository springDataRepo;
    private final StringRedisTemplate redisTemplate;

    protected OrganizationRepositoryAdapter(SpringDataOrganizationRepository springDataRepository,
                                            OrganizationMapper mapper,
                                            DomainSequenceGenerator sequenceGenerator,
                                            DomainEventPublisher eventPublisher,
                                            StringRedisTemplate redisTemplate) {
        super(springDataRepository, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected String getSequenceName() {
        return "organization_seq";
    }

    @Override
    public List<Organization> findAllByIds(List<Long> ids) {
        return springDataRepo.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Organization save(Organization entity) {
        Organization saved = super.save(entity);
        evictOrganization(saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        evictOrganization(id);
        super.deleteById(id);
    }

    private void evictOrganization(Long orgId) {
        try {
            redisTemplate.delete(OrganizationQueryPortAdapter.ORGANIZATION_BY_ID_KEY_PREFIX + orgId);
        } catch (RuntimeException ignored) {
        }
    }
}
