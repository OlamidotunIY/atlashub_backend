package com.atlashub.iam.application.services;

import com.atlashub.iam.domain.valueobject.PermissionAction;

import java.util.List;

public final class PlatformPermissionCatalog {
    private PlatformPermissionCatalog() {}

    public static List<Definition> definitions() {
        return List.of(
                permission("pay:accounts:create", "Issue reserved accounts"),
                permission("pay:accounts:read", "View banking accounts"),
                permission("pay:accounts:suspend", "Suspend reserved accounts"),
                permission("pay:accounts:reactivate", "Reactivate reserved accounts"),
                permission("pay:accounts:close", "Close reserved accounts"),
                permission("pay:charges:create", "Initiate payment collection"),
                permission("pay:transfers:create", "Initiate bank transfers"),
                permission("pay:transfers:approve", "Approve bank transfers"),
                permission("pay:ledger:read", "View ledger transactions"),
                permission("pay:splits:manage", "Manage split rules"),
                permission("pay:settlements:read", "View settlements"),
                permission("commerce:products:manage", "Manage products"),
                permission("commerce:orders:create", "Create orders"),
                permission("commerce:orders:read", "View orders"),
                permission("commerce:orders:refund", "Refund orders"),
                permission("commerce:inventory:read", "View inventory"),
                permission("commerce:inventory:update", "Update inventory"),
                permission("commerce:suppliers:manage", "Manage suppliers"),
                permission("commerce:pos:manage", "Manage POS"),
                permission("commerce:tills:open", "Open tills"),
                permission("commerce:tills:close", "Close tills"),
                permission("commerce:vendors:manage", "Manage marketplace vendors"),
                permission("logistics:shipments:create", "Create shipments"),
                permission("logistics:shipments:dispatch", "Dispatch shipments"),
                permission("logistics:shipments:read", "View shipments"),
                permission("logistics:fleet:manage", "Manage fleet"),
                permission("logistics:riders:manage", "Manage riders"),
                permission("logistics:transfers:approve", "Approve stock transfers"),
                permission("hr:employees:manage", "Manage employees"),
                permission("hr:employees:read", "View employees"),
                permission("hr:payroll:initiate", "Initiate payroll"),
                permission("hr:payroll:approve", "Approve payroll"),
                permission("hr:payroll:read", "View payroll"),
                permission("hr:leave:approve", "Approve leave"),
                permission("hr:loans:approve", "Approve employee loans"),
                permission("accounting:journal:post", "Post journal entries"),
                permission("accounting:journal:approve", "Approve journal entries"),
                permission("accounting:reports:read", "View accounting reports"),
                permission("accounting:accounts:manage", "Manage chart of accounts"),
                permission("accounting:expenses:approve", "Approve expenses"),
                permission("accounting:budget:manage", "Manage budgets"),
                permission("iam:members:invite", "Invite organization members"),
                permission("iam:members:manage", "Manage organization members"),
                permission("iam:roles:manage", "Manage custom roles"),
                permission("iam:apikeys:manage", "Manage API keys"),
                permission("compliance:read", "View compliance status"),
                permission("compliance:manage", "Manage compliance information"),
                permission("compliance:submit", "Submit compliance applications")
        );
    }

    private static Definition permission(String code, String description) {
        String[] parts = code.split(":");
        if (parts.length != 2 && parts.length != 3) {
            throw new IllegalArgumentException("Permission code must use module:action or module:resource:action format: " + code);
        }
        String resource = parts.length == 2 ? parts[0] : parts[1];
        String action = parts[parts.length - 1];
        return new Definition(code, parts[0], resource, PermissionAction.valueOf(action.toUpperCase()),
                description, description);
    }

    public record Definition(String code, String module, String resource, PermissionAction action,
                             String displayName, String description) {}
}
