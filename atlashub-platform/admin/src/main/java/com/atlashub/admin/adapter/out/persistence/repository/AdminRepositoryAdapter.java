package com.atlashub.admin.adapter.out.persistence.repository;

import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.valueobject.AdminRole;
import com.atlashub.admin.domain.repository.AdminRepository;
import com.atlashub.admin.adapter.out.persistence.mapper.AdminMapper;
import com.atlashub.shared.adapter.out.external.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminRepositoryAdapter implements AdminRepository {

    private final SpringDataAdminRepository jpaRepository;
    private final AdminMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public AdminRepositoryAdapter(SpringDataAdminRepository jpaRepository, AdminMapper mapper, DomainSequenceGenerator sequenceGenerator) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Admin save(Admin admin) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(admin)));
    }

    @Override
    public Optional<Admin> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByRole(AdminRole role) {
        return jpaRepository.existsByRole(role);
    }

    @Override
    public Optional<Admin> findByRole(AdminRole role) {
        return jpaRepository.findByRole(role).map(mapper::toDomain);
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("admin_seq");
    }
}
