# atlashub Frontend API Context

This document outlines the current state of the backend APIs across all modules. It is designed to serve as the context for frontend integration.

## General Response Format

All REST endpoints return a standardized ApiResponse<T> structure:

``json
{
  "status": true,
  "message": "Human readable success/error message",
  "data": { ... }, // Payload of type T. Can be an object, array, or null.
  "meta": {        // Included for paginated endpoints
    "total": 100,
    "skipped": 0,
    "perPage": 20,
    "page": 1,
    "pageCount": 5
  }
}
``

## REST API Authentication & Headers
For REST APIs that require authentication, you must include the JWT token in the Authorization header:
Authorization: Bearer <your_access_token>

For internal Merchant scoping (when logged in as a Merchant), pass the X-Merchant-Id header (usually retrieved from the JWT token claims or user profile state on the frontend):
X-Merchant-Id: 12345

For Admins managing specific resources, pass X-Admin-Id:
X-Admin-Id: 67890

---

## 1. Auth Module (/api/v1/auth)

Handles authentication for both Merchants and Admins.

- **POST /api/v1/auth/login**
  - **Request:** { "identifier": "string", "password": "string" }
  - **Response:** AuthResponseDto (contains accessToken, refreshToken, nextAction, status)
  - **Note:** Supports login via email (Merchants) or Employee Code (Admins).

- **POST /api/v1/auth/password/change**
  - **Request:** { "identifier": "string", "oldPassword": "string", "newPassword": "string" }
  - **Response:** AuthResponseDto

- **POST /api/v1/auth/setup-password**
  - **Request:** { "setupToken": "string", "newPassword": "string" }
  - **Response:** AuthResponseDto

- **POST /api/v1/auth/setup-password/resend**
  - **Request:** { "identifier": "string" }
  - **Response:** Void (Data is null)

- **POST /api/v1/auth/verify-email**
  - **Request:** { "type": "MERCHANT", "identifier": "string", "code": "string" }
  - **Response:** VerificationResponseDto

- **POST /api/v1/auth/mfa/verify**
  - **Request:** { "preAuthToken": "string", "code": "string" }
  - **Response:** AuthTokenDto

- **POST /api/v1/auth/refresh**
  - **Request:** { "refreshToken": "string" }
  - **Response:** AuthTokenDto

- **POST /api/v1/auth/logout**
  - **Request:** { "jti": "string" }
  - **Response:** Void

---

## 2. Admin Module (/api/v1/admins)

Manages internal atlashub administrators.

- **POST /api/v1/admins/auth/bootstrap**
  - **Request:** { "username": "string" }
  - **Response:** AdminCreationResult
  - **Note:** Sets up the initial master admin and Cloudflare routing. Run once.

- **POST /api/v1/admins**
  - **Request:** { "username": "string", "destinationEmail": "string", "permissions": ["MANAGE_MERCHANTS", "MANAGE_ADMINS"] }
  - **Response:** AdminCreationResult
  - **Note:** Requires X-Admin-Id. Creates a standard admin with permissions.

---

## 3. Identity Module (/api/v1/...)

Manages Merchant onboarding, profiles, compliance, sub-accounts, and API keys.

### Users (/api/v1/users)
- **POST /api/v1/users**
  - **Request:** { firstName, lastName, email, phone, country }
  - **Response:** CreateUserResult (contains userId)
- **GET /api/v1/users/{userId}** -> UserDto
- **GET /api/v1/users?page=1&size=20** -> Array of UserDto

### Organizations (/api/v1/Organizations)
- **POST /api/v1/Organizations**
  - **Request:** { businessName, businessType }
  - **Response:** RegisterOrganizationResult (contains organizationId)
- **GET /api/v1/Organizations/profile** -> OrganizationProfileDto
- **GET /api/v1/Organizations/compliance** -> ComplianceStatus
- **PUT /api/v1/Organizations/compliance/profile** -> Void
- **PUT /api/v1/Organizations/compliance/contact** -> Void
- **PUT /api/v1/Organizations/compliance/owner** -> Void
- **PUT /api/v1/Organizations/compliance/account** -> Void
- **PUT /api/v1/Organizations/compliance/service-agreement** -> Void
- **POST /api/v1/Organizations/compliance/submit** -> Void

### Invitations (/api/v1/invitations)
- **POST /api/v1/organizations/{organizationId}/invitations**
  - **Request:** { email, role }
  - **Response:** Void
- **GET /api/v1/invitations/{token}** -> InvitationDto
- **POST /api/v1/invitations/{token}/accept** -> Void (For existing users)
- **POST /api/v1/invitations/{token}/decline** -> Void

### Sub-Accounts (/api/v1/subaccounts)
- **POST /api/v1/subaccounts** -> RegisterSubAccountResult
- **GET /api/v1/subaccounts/{subAccountId}** -> SubAccountDto
- **GET /api/v1/subaccounts?page=1&size=20** -> Array of SubAccountDto

### API Keys (/api/v1/keys)
- **GET /api/v1/keys** -> { keys: [...] }
- **POST /api/v1/keys/regenerate** -> { rawKey: "..." }
- **DELETE /api/v1/keys/{keyId}** -> { keyId: "...", active: false }

---

## 4. Accounts Module (/api/v1/dedicated_account)

Manages virtual accounts for merchants, mapped directly to Anchor API Sandbox.

- **POST /api/v1/dedicated_account**
  - **Request:** { customerCode, accountName, bankName, currency, idempotencyKey } (Header X-Merchant-Id)
  - **Response:** String (the accountId)

- **GET /api/v1/dedicated_account**
  - **Request:** (Header X-Merchant-Id)
  - **Response:** Array of Virtual Accounts

---

## 5. Ledger Module (/api/v1/balance)

Financial tracking and history.

- **GET /api/v1/balance**
  - **Request:** (Header X-Merchant-Id)
  - **Response:** Array of BalanceDto

- **GET /api/v1/balance/ledger?page=1&perPage=50**
  - **Request:** (Header X-Merchant-Id)
  - **Response:** Array of LedgerHistoryDto

---

## 6. WebSockets / Real-Time Events (Notifications Module)

atlashub uses an Enterprise Ticket-based Handshake for WebSockets to secure the connection and avoid placing JWTs in the query params.

### How to Connect (Frontend WebSocket Flow)

1. **Request a WebSocket Ticket (REST API)**
   - **Method**: POST
   - **Endpoint**: /api/v1/ws/ticket
   - **Header**: Authorization: Bearer <access_token>
   - **Response**: { "status": true, "data": { "ticket": "uuid-string-here" } }
   - **Note**: This ticket expires in 30 seconds.

2. **Connect via STOMP**
   - Connect using SockJS or native WebSocket to /ws-events?ticket=uuid-string-here
   - The server validates the ticket from Redis and securely maps the connection to the logged-in user.

3. **Subscribe to Private Events**
   - Subscribe to the destination: /user/queue/events
   - This channel broadcasts all Kafka domain events mapped to your specific user/merchant identity (e.g. TransferCompletedEvent, MerchantRegisteredEvent, VirtualAccountFundedEvent).
