package com.atlashub.admin.domain.repository;

import com.atlashub.admin.domain.model.Admin;
import com.atlashub.admin.domain.model.AdminRole;
import java.util.Optional;

public interface AdminRepository {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(AdminRole role);
    Long nextIdentity();
}
