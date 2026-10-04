# RBAC Architecture Design

> **Scope**: This document covers the Role-Based Access Control system for AtlasHub — how permissions are stored, how they reach the JWT, and how they are enforced inside each module's Handler layer.

---

## 1. Overview

AtlasHub uses a **JWT-claim-based RBAC** model. At login the `auth` module loads the user's effective permissions from the `iam` module and embeds them as an array in the JWT. Every downstream request carries its own permissions — no database call is needed during request processing.

Spring Security's `@PreAuthorize("hasAuthority('...')")` enforces those permissions at the Handler layer, not the controller layer.

```
Login Request
    ↓
auth: AuthenticateHandler
    → loads OrganizationMember (role, customRoleId) from iam
    → fetches Permission set for that role
    → encodes permissions[] into JWT claims
    ↓
JWT issued to client
    ↓
Every subsequent request
    → JwtAuthenticationFilter extracts userId + permissions[]
    → builds UsernamePasswordAuthenticationToken with GrantedAuthority list
    → Spring Security populates SecurityContext
    ↓
Handler.execute() — @PreAuthorize("hasAuthority('pay:transfers:approve')")
    → Spring evaluates GrantedAuthority list
    → allows or throws AccessDeniedException (→ 403)
```

---

## 2. Where RBAC Data Lives: `atlashub-platform:iam`

All permission and role data is owned by the `iam` module.

### `Permission` (Entity)
Platform-seeded. Organizations cannot create their own permissions.

```
Permission
├── id: Long
├── code: String          ← e.g., "pay:transfers:approve"
├── name: String          ← e.g., "Approve Outbound Transfers"
├── module: String        ← e.g., "pay"
└── description: String
```

Permissions are synchronized idempotently from `PlatformPermissionCatalog` at startup by `PermissionCatalogInitializer`, which delegates to `SynchronizePermissionsHandler`. No UI creates platform permissions.

### `CustomRole` (Entity)
Each organization can have multiple custom roles.

```
CustomRole
├── id: Long
├── organizationId: Long
├── name: String          ← e.g., "Payroll Officer"
├── isSystemDefault: Boolean
└── permissionIds: Set<Long>   ← references to Permission.id
```

The built-in role created per organization on `OrganizationRegistered` is:

| Role Type | Permissions |
|---|---|
| `OWNER` | Every active platform permission, resolved dynamically |

All non-owner roles are organization-defined custom roles. AtlasHub does not seed fixed `ADMIN` or `MEMBER` roles.

### `OrganizationMember` (Entity)
```
OrganizationMember
├── id: Long
├── userId: Long
├── organizationId: Long
├── customRoleId: Long          ← OWNER role or an organization-defined custom role
├── status: MemberStatus
└── joinedAt: ZonedDateTime
```

- If the referenced role is the immutable built-in OWNER role, effective permissions are every active catalog permission, including permissions added later.
- Otherwise, effective permissions are the active permissions assigned to the referenced custom role.

---

## 3. JWT Structure

The auth module issues a JWT with the following claims:

```json
{
  "sub": "12345",
  "userId": 12345,
  "activeOrganizationId": 67,
  "permissions": [
    "pay:transfers:initiate",
    "pay:transfers:approve",
    "hr:payroll:read",
    "iam:members:invite"
  ],
  "iat": 1727391600,
  "exp": 1727478000,
  "jti": "a1b2c3d4-..."
}
```

The `permissions` array is the flattened set of all permission codes the user holds in their active organization context.

---

## 4. Spring Security Setup (`atlashub-main`)

### `SecurityConfig`
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

Key settings:
- `SessionCreationPolicy.STATELESS` — no server-side sessions, JWT only.
- `@EnableMethodSecurity(prePostEnabled = true)` — activates `@PreAuthorize` on any Spring bean.
- `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`.

### `JwtAuthenticationFilter`
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtVerifier jwtVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                JwtClaims claims = jwtVerifier.verify(header.substring(7));

                List<GrantedAuthority> authorities = claims.permissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        claims.userId(),   // principal — Long userId
                        null,
                        authorities
                    );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
```

The `principal` in the SecurityContext is the `userId` (`Long`). Controllers extract it via `@AuthenticationPrincipal Long userId`.

---

## 5. Permission Enforcement at the Handler Layer

`@PreAuthorize` is placed on the `execute()` method of the Handler, **not** on the controller. This ensures enforcement regardless of how the handler is called — HTTP request, another handler, or an internal service.

```java
@Component
public class ApprovePayout extends Command<ApprovePayoutCommand, ApprovePayoutResponse> {

    @Override
    @PreAuthorize("hasAuthority('pay:transfers:approve')")
    public ApprovePayoutResponse execute(ApprovePayoutCommand command) {
        // ...
    }
}
```

### Rules for Placement
| Handler Type | Gets @PreAuthorize? | Reason |
|---|---|---|
| User-triggered command handler | **Yes** | User action — enforce permission |
| Admin-triggered command handler | **Yes** | Admin action — enforce admin permission |
| Kafka listener-invoked handler | **No** | System action — no user context in SecurityContext |
| Scheduler-invoked handler | **No** | System job — runs without a user session |
| Query handler (user-facing) | **Yes** | Reading sensitive data requires read permission |
| Query handler (public) | Annotated `@PublicEndpoint` on controller, no `@PreAuthorize` on handler |

### When a Permission Is Missing
Spring Security throws `AccessDeniedException`, which the `GlobalExceptionHandler` in `atlashub-main` maps to `HTTP 403 Forbidden`:
```json
{
  "success": false,
  "message": "Access denied — insufficient permissions",
  "data": null,
  "error": "FORBIDDEN"
}
```

---

## 6. Permission Naming Convention

**Format**: `module:resource:action`

| Segment | Rule | Example |
|---|---|---|
| `module` | Gradle module name without `atlashub-` prefix | `pay`, `hr`, `iam`, `compliance` |
| `resource` | Domain entity/concept — plural noun | `transfers`, `members`, `payroll`, `records` |
| `action` | Verb — what the user is doing | `create`, `read`, `approve`, `revoke`, `initiate` |

### Approved Action Verbs
`create` · `read` · `update` · `delete` · `initiate` · `approve` · `reject` · `assign` · `revoke` · `export` · `manage`

### Full Permission Registry (by module)

| Module | Permission Code | Description |
|---|---|---|
| `iam` | `iam:members:invite` | Invite a new organization member |
| `iam` | `iam:members:read` | View member list and profiles |
| `iam` | `iam:members:manage` | Update members, deactivate |
| `iam` | `iam:roles:manage` | Create and edit custom roles |
| `iam` | `iam:api-keys:manage` | Create / revoke API keys |
| `pay` | `pay:transfers:initiate` | Initiate an outbound bank transfer |
| `pay` | `pay:transfers:approve` | Approve a pending payout |
| `pay` | `pay:transfers:read` | View transaction history |
| `pay` | `pay:charges:initiate` | Create a charge (POS / API) |
| `pay` | `pay:wallets:read` | View wallet balances |
| `hr` | `hr:employees:manage` | Onboard, update, terminate employees |
| `hr` | `hr:employees:read` | View employee list and profiles |
| `hr` | `hr:payroll:initiate` | Initiate a payroll run |
| `hr` | `hr:payroll:approve` | Approve a payroll run |
| `hr` | `hr:payroll:read` | View payroll history |
| `hr` | `hr:leave:manage` | Approve / reject leave applications |
| `compliance` | `compliance:records:submit` | Submit compliance documents |
| `compliance` | `compliance:records:approve` | Approve compliance submissions (admin) |
| `compliance` | `compliance:records:read` | View compliance status |
| `commerce` | `commerce:inventory:manage` | Adjust stock levels |
| `commerce` | `commerce:products:manage` | Create and edit products |
| `commerce` | `commerce:sales:read` | View sales reports |
| `accounting` | `accounting:journals:initiate` | Create a manual journal entry |
| `accounting` | `accounting:journals:approve` | Approve a journal entry |
| `accounting` | `accounting:reports:read` | View financial reports |
| `support` | `support:tickets:create` | Open a support ticket |
| `support` | `support:tickets:assign` | Assign a ticket to an agent |
| `support` | `support:tickets:resolve` | Resolve a ticket |
| `logistics` | `logistics:shipments:manage` | Create and manage shipments |
| `logistics` | `logistics:shipments:read` | View shipment status |

---

## 7. Adding a New Permission

When a new handler requires a permission:

1. **Add to `PlatformPermissionCatalog`** in the `iam` module.
2. **Add `@PreAuthorize`** to the handler's `execute()` method.
3. **Update the module doc's RBAC table** with the new permission.
4. No OWNER update is required: OWNER resolves every active permission dynamically.

No migrations or schema changes are needed — startup synchronization creates only missing permission codes.

---

## 8. Built-In Role Coverage

OWNER has every active catalog permission. Custom-role coverage is selected per organization and cannot be empty.

---

## 9. Custom Roles

Organization admins can define custom roles with arbitrary permission subsets. Custom roles allow fine-grained delegation — e.g., a "Payroll Officer" with only `hr:payroll:initiate` + `hr:employees:read` but no `pay:transfers:approve`.

### Creating a Custom Role
```
POST /api/v1/iam/roles
{
  "name": "Payroll Officer",
  "permissionCodes": ["hr:payroll:initiate", "hr:employees:read", "hr:payroll:read"]
}
```

Handled by `CreateCustomRoleHandler` in `atlashub-platform:iam` (`@PreAuthorize("hasAuthority('iam:roles:manage')")`).

### Assigning a Custom Role
```
PATCH /api/v1/iam/members/{memberId}/role
{
  "customRoleId": 42
}
```

Handled by `AssignRoleHandler` under a pessimistic member lock. Permission changes publish `CustomRolePermissionsChangedEvent`; Authentication revokes organization sessions so replacement tokens contain current permissions.

---

## 10. IAM Infrastructure: Confirmation from Source

The following files are confirmed present in `atlashub-platform/iam/src/main/java/com/atlashub/iam/infrastructure`:

```
persistence/adapters/
  ApiKeyRepositoryAdapter.java
  CustomRoleRepositoryAdapter.java
  InvitationRepositoryAdapter.java
  OrganizationMemberRepositoryAdapter.java
  PermissionRepositoryAdapter.java
persistence/entities/
  ApiKeyJpa.java
  CustomRoleJpa.java
  InvitationJpa.java
  OrganizationMemberJpa.java
  PermissionJpa.java
persistence/mappers/
  ApiKeyMapper.java
  CustomRoleMapper.java
  InvitationMapper.java
  OrganizationMemberMapper.java
  PermissionMapper.java
persistence/repositories/
  SpringDataApiKeyRepository.java
  SpringDataCustomRoleRepository.java
  SpringDataInvitationRepository.java
  SpringDataOrganizationMemberRepository.java
  SpringDataPermissionRepository.java
services/
  MembershipQueryAdapter.java
messaging/listeners/
  OrganizationCreatedListener.java
  SubscriptionSuspendedListener.java
  EmployeeSuspendedListener.java
  OrganizationBannedListener.java
  ApiKeyAuthenticatedListener.java
messaging/schedulers/
  InvitationExpirationScheduler.java
```

`MembershipQueryAdapter` implements the `MembershipQueryPort` used by maker-checker handlers to count approvers. `PermissionRepositoryAdapter` serves the permission seeder and the auth module's permission-loading query.

---

## 11. Best Practices

- **Never put RBAC logic in controllers.** Controllers extract `userId` from `@AuthenticationPrincipal` and pass it into the command. The handler enforces permissions.
- **Never add `@PreAuthorize` to listener-invoked handlers.** Kafka listeners run outside the HTTP security context — Spring Security has no principal to evaluate. Listener-triggered handlers must not be gated by permissions.
- **Cache JWT claims aggressively.** Permission data changes infrequently. A 15-minute JWT expiry with a Redis token revocation check is the correct approach — do not re-query the database on every request.
- **Fail safe.** A handler with no `@PreAuthorize` is accessible by any authenticated user. The default posture for user-facing operations should be: protected unless explicitly marked public.
