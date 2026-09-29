package com.atlashub.iam.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.iam.domain.entities.Permission;
import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface PermissionRepository extends Repository<Permission> {
    List<Permission> findAllById(Iterable<Long> ids);
    List<Permission> findByModule(String module);
}
