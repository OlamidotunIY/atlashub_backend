# Enterprise RBAC Architecture: Platform vs. Tenant

This document defines the dual Role-Based Access Control (RBAC) architecture for AtlasHub. It enforces a strict separation of concerns between **System Administrators** (who manage the platform infrastructure) and **Organization Members** (who manage tenant-specific business data).

---

## 1. The Dual-Identity Security Model

A fundamental security rule in AtlasHub is that **System Admins do not have implicit access to tenant data**. The security context is evaluated using two entirely different mechanisms based on the module being accessed.

| Context | Module Owning Identity | Security Evaluator | Target Audience | Example Action |
| :--- | :--- | :--- | :--- | :--- |
| **Platform** | `atlashub-platform:admin` | `@adminAuth` | AtlasHub Employees | Create `HubProduct` |
| **Tenant** | `atlashub-platform:identity` | `@tenantAuth` | B2B Customers | Update `Inventory` |

---

## 2. Platform RBAC Implementation (`admin` module)

The `admin` module already possesses `Admin`, `AdminRole`, and `AdminPermission`. Because platform roles are static (defined by AtlasHub, not by customers), static enumerations and value objects are perfectly acceptable here.

### Missing Configurations & Implementation Steps
1. **Admin JWT Differentiation**: The Authentication server must issue JWTs that explicitly identify the token as an `ADMIN_TOKEN`.
2. **The Evaluator Bean**: Create a Spring component to evaluate platform permissions.
   ```java
   @Component("adminAuth")
   public class AdminSecurityEvaluator {
       private final AdminRepository adminRepository;

       public boolean hasPermission(Long adminId, String requiredPermission) {
           return adminRepository.findById(adminId)
               .map(admin -> admin.getRole().getPermissions().contains(requiredPermission))
               .orElse(false);
       }
   }
   ```
3. **Use Case Protection**:
   ```java
   @PreAuthorize("@adminAuth.hasPermission(#command.adminId, 'CATALOG_MANAGE')")
   public HubProductResult execute(CreateHubProductCommand command) { ... }
   ```

---

## 3. Tenant Dynamic RBAC Implementation (`identity` module)

### Where should Tenant RBAC live?
Tenant roles and permissions should live inside the `atlashub-platform:identity` module. 
*Why?* The `identity` module already manages the `Organization`, `User`, and `OrganizationMember` boundaries. RBAC is simply an extension of the `OrganizationMember` identity. Introducing a separate IAM module would create unnecessary distributed queries just to check a user's role.

### Missing Configurations & Required Entities
To support Dynamic Custom Roles (where organizations define their own access profiles), the `identity` module must be updated to replace the static `OrganizationRole` enum with database-backed aggregates.

**1. New Entities Required in `identity` Domain**:
- **`CustomRole` (Aggregate)**:
  - `id`: Long
  - `organizationId`: Long
  - `name`: String (e.g., "Weekend Cashier")
  - `description`: String
  - `isSystemDefault`: Boolean (e.g., true for the undeletable 'OWNER' role)
- **`RolePermission` (Value Object / Entity)**:
  - `roleId`: Long
  - `permissionKey`: String (e.g., `inventory:write`, `pos:refund`)

**2. Updates to Existing Entities**:
- **`OrganizationMember`**: Must drop the static `OrganizationRole` enum and instead maintain a relationship to a `roleId` (or a list of `roleIds` if members can have multiple roles).

**3. The Evaluator Bean**:
   ```java
   @Component("tenantAuth")
   public class TenantSecurityEvaluator {
       private final OrganizationMemberRepository memberRepo;
       private final CustomRoleRepository roleRepo;

       public boolean hasPermission(Long userId, Long orgId, String requiredPermission) {
           // 1. Fetch the user's membership in the target organization
           Optional<OrganizationMember> member = memberRepo.findByUserIdAndOrganizationId(userId, orgId);
           if (member.isEmpty()) return false;

           // 2. Fetch the custom role assigned to that member
           Optional<CustomRole> role = roleRepo.findById(member.get().getRoleId());
           
           // 3. Check if the role contains the specific permission string
           return role.map(r -> r.getPermissions().contains(requiredPermission)).orElse(false);
       }
   }
   ```

### 4. Implementation Steps for Tenant RBAC
1. **Seed Permissions**: Define a static dictionary (Enum or Config) of all *possible* permissions in the system (e.g., `inventory:read`, `staff:manage`). This is used to populate the UI checkboxes when an Organization Admin creates a custom role.
2. **Default Roles**: When an `OrganizationRegistered` event fires, the system should automatically create default `CustomRole`s (e.g., "Owner", "Admin", "Staff") for that specific `organizationId` and grant the creator the "Owner" role.
3. **Use Case Protection**:
   ```java
   @PreAuthorize("@tenantAuth.hasPermission(#command.userId, #command.organizationId, 'inventory:write')")
   public InventoryResult execute(AdjustStockCommand command) { ... }
   ```

---

## 5. Summary of Best Practices
- **No Controller Business Logic**: Controllers only extract `adminId` or `userId` and `orgId` from the JWT and pass them to the Command.
- **Fail-Safe Security**: By placing `@PreAuthorize` on the `execute` method of the `BaseUseCase`, the authorization rules are rigidly enforced regardless of whether the action was triggered by an HTTP request, a Kafka Event, or a Cron Job.
- **Cache Heavily**: The `@tenantAuth` and `@adminAuth` evaluators will be invoked on almost every request. The results of `adminRepository.findById` and `memberRepo.findByUserIdAndOrganizationId` must be cached (e.g., Redis) using Spring `@Cacheable` to prevent severe database degradation.
