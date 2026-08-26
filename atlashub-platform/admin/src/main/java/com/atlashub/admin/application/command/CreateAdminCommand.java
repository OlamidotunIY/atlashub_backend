package com.atlashub.admin.application.command;

import com.atlashub.admin.domain.model.AdminPermission;
import java.util.Set;

public record CreateAdminCommand(
    Long requestingAdminId, 
    String username, 
    String destinationEmail, 
    Set<AdminPermission> initialPermissions
) {}
