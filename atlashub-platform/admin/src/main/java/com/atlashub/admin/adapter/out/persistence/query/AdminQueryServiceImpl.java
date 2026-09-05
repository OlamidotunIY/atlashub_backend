package com.atlashub.admin.adapter.out.persistence.query;

import com.atlashub.admin.adapter.out.persistence.repository.SpringDataAdminRepository;
import com.atlashub.admin.application.port.AdminQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminQueryServiceImpl implements AdminQueryService {

    private final SpringDataAdminRepository adminRepository;

    @Override
    public Optional<AdminDto> getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .map(admin -> new AdminDto(
                        admin.getId(), admin.getEmail(), admin.getRole().name(), admin.getUsername()
                ));
    }
}
