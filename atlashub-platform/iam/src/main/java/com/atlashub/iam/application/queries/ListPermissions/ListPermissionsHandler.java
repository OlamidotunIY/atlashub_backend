package com.atlashub.iam.application.queries.ListPermissions;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.entities.Permission;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListPermissionsHandler extends Query<ListPermissionsQuery, List<PermissionResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListPermissionsHandler.class);
    
    private final PermissionRepository permissionRepository;

    public ListPermissionsHandler(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<PermissionResult> execute(ListPermissionsQuery query) {
        log.info("Executing ListPermissionsQuery for module: {}", query.moduleName());
        
        List<Permission> permissions = permissionRepository.findByModule(query.moduleName());

        return permissions.stream()
            .map(PermissionResult::fromEntity)
            .collect(Collectors.toList());
    }
}
