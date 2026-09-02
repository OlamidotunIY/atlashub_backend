package com.atlashub.admin.adapter.in.internal;

import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.repository.AdminRepository;
import com.atlashub.shared.application.api.AdminQueryApi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InternalAdminApiImpl implements AdminQueryApi {

    private final AdminRepository adminRepository;

    public InternalAdminApiImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public Optional<AdminSharedDto> getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
            .map(admin -> new AdminSharedDto(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail().value()
            ));
    }
}
