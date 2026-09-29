package com.atlashub.iam.application.queries.GetCustomRolePermissions;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GetCustomRolePermissionsHandler extends Query<GetCustomRolePermissionsQuery, CustomRolePermissionsResult> {

    private static final Logger log = LoggerFactory.getLogger(GetCustomRolePermissionsHandler.class);

    private final CustomRoleRepository customRoleRepository;
    private final PermissionRepository permissionRepository;

    public GetCustomRolePermissionsHandler(
            CustomRoleRepository customRoleRepository,
            PermissionRepository permissionRepository) {
        this.customRoleRepository = customRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public CustomRolePermissionsResult execute(GetCustomRolePermissionsQuery query) {
        log.info("Executing GetCustomRolePermissionsQuery for roleId: {}", query.roleId());
        
        CustomRole customRole = customRoleRepository.findById(query.roleId())
                .orElseThrow(() -> new NotFoundException("Custom role not found with id: " + query.roleId()));

        List<CustomRolePermissionsResult.PermissionDetail> permissionDetails = new ArrayList<>();
        
        for (Long permissionId : customRole.getPermissions()) {
            Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);
            if (permissionOpt.isPresent()) {
                Permission permission = permissionOpt.get();
                permissionDetails.add(new CustomRolePermissionsResult.PermissionDetail(
                        permission.getId(),
                        permission.getCode(),
                        permission.getModule(),
                        permission.getResource(),
                        permission.getAction() != null ? permission.getAction().name() : null,
                        permission.getDisplayName(),
                        permission.getDescription()
                ));
            }
        }
        
        return new CustomRolePermissionsResult(customRole.getId(), permissionDetails);
    }
}
