package com.atlashub.iam.application.queries.ListCustomRoles;

import com.atlashub.shared.application.usecase.Query;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.entities.CustomRole;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListCustomRolesHandler extends Query<ListCustomRolesQuery, List<CustomRoleResult>> {

    private static final Logger log = LoggerFactory.getLogger(ListCustomRolesHandler.class);

    private final CustomRoleRepository customRoleRepository;

    public ListCustomRolesHandler(CustomRoleRepository customRoleRepository) {
        this.customRoleRepository = customRoleRepository;
    }

    @Override
    public List<CustomRoleResult> execute(ListCustomRolesQuery query) {
        log.info("Executing ListCustomRolesQuery");
        
        List<CustomRole> roles = customRoleRepository.findByOrganizationId(query.orgId());
        
        List<CustomRoleResult> results = roles.stream()
            .map(role -> new CustomRoleResult(
                role.getId(),
                role.getOrganizationId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions(),
                role.isBuiltIn(),
                role.getCreatedBy(),
                role.getCreatedAt(),
                role.getUpdatedAt()
            ))
            .collect(Collectors.toList());
            
        return results;
    }
}

