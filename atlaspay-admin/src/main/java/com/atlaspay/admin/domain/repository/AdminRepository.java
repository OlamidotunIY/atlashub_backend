package com.atlaspay.admin.domain.repository;

import com.atlaspay.admin.domain.model.Admin;
import com.atlaspay.admin.domain.model.AdminRole;
import java.util.Optional;

public interface AdminRepository {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(AdminRole role);
    Long nextIdentity();
}
