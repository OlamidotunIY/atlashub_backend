package com.atlashub.iam.application.queries.ListPermissions;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.entities.Permission;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Component
public class ListPermissionsHandler extends Query<ListPermissionsQuery, List<PermissionResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListPermissionsHandler.class);
    
    private final PermissionRepository permissionRepository;

    public ListPermissionsHandler(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('iam:roles:manage')")
    public List<PermissionResult> execute(ListPermissionsQuery query) {
        log.info("Executing ListPermissionsQuery for module: {}", query.moduleName());
        
        List<Permission> permissions = query.moduleName() == null || query.moduleName().isBlank()
                ? permissionRepository.findAllByActiveTrue()
                : permissionRepository.findByModule(query.moduleName()).stream().filter(Permission::isActive).toList();

        return permissions.stream()
            .map(PermissionResult::fromEntity)
            .collect(Collectors.toList());
    }
}
