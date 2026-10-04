package com.atlashub.iam.application.queries.GetCustomRolePermissions;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.Permission;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.iam.domain.exception.CustomRoleNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

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
    @PreAuthorize("hasAuthority('iam:roles:manage')")
    public CustomRolePermissionsResult execute(GetCustomRolePermissionsQuery query) {
        log.info("Executing GetCustomRolePermissionsQuery for roleId: {}", query.roleId());
        
        CustomRole customRole = customRoleRepository.findById(query.roleId())
                .filter(found -> found.getOrganizationId().equals(query.organizationId()))
                .orElseThrow(CustomRoleNotFoundException::new);

        List<Permission> permissions = customRole.isBuiltIn()
                ? permissionRepository.findAllByActiveTrue()
                : permissionRepository.findAllById(customRole.getPermissions()).stream()
                        .filter(Permission::isActive)
                        .toList();
        List<CustomRolePermissionsResult.PermissionDetail> permissionDetails = permissions.stream()
                .map(permission -> new CustomRolePermissionsResult.PermissionDetail(
                        permission.getId(), permission.getCode(), permission.getModule(), permission.getResource(),
                        permission.getAction() != null ? permission.getAction().name() : null,
                        permission.getDisplayName(), permission.getDescription()))
                .toList();
        
        return new CustomRolePermissionsResult(customRole.getId(), permissionDetails);
    }
}
