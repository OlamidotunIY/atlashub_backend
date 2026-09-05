package com.atlashub.admin.adapter.in.internal;

import com.atlashub.admin.application.port.AdminQueryService;
import com.atlashub.admin.domain.repository.AdminRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InternalAdminApiImpl implements AdminQueryService {

    private final AdminRepository adminRepository;

    public InternalAdminApiImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public Optional<AdminDto> getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .map(admin -> new AdminDto(
                        admin.getId(),
                        admin.getEmail().value(),
                        admin.getRole().name(),
                        admin.getUsername()
                ));
    }
}
