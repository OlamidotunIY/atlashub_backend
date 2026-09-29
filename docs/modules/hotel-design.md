# Hotel Module Design (`atlashub-hotel`)

> [!CAUTION]
> **STATUS: NOT IN MVP.**
> This document describes the architectural vision for the Hotel PMS (Property Management System) module. **No code will be written for this module in the current development cycle.**
>
> Full application layer, infrastructure, and presentation documentation will be written when this module enters active development.
>
> This document exists so that when development begins, the domain has been correctly thought through and is not designed retroactively.

> [!NOTE]
> **Restaurant / hospitality POS** (table management, kitchen order tickets, F&B billing) **is part of `atlashub-commerce`** and **IS in the MVP**. Only the Hotel PMS is deferred.

---

## Role & Purpose

`atlashub-hotel` is a full **Hotel Property Management System (PMS)** designed for African hotels — from boutique guesthouses to large resort chains. It integrates natively with all other AtlasHub modules:

| Integration | Module | Purpose |
|---|---|---|
| Financial transactions | `atlashub-pay` | Deposits, room charges, folio settlements, refunds |
| Restaurant & bar operations | `atlashub-commerce` | Table management, KOTs, F&B billing posted to guest folio |
| Revenue journal entries | `atlashub-accounting` | Nightly room revenue auto-post |
| Staff management | `atlashub-hr` | Front desk agents, housekeepers, F&B staff |
| Shuttle dispatch | `atlashub-logistics` | Airport / inter-property vehicle dispatch |

The competitive advantage over existing African hotel PMS solutions (Opera, Protel) is **deep integration with a payment and business management platform** — no separate POS system, no separate accounting software, no separate HR tool.

---

## 1. Features (Planned)

### `frontdesk` Submodule
- Walk-in and advance reservations
- Guest check-in and check-out
- Room assignment and room moves
- Bill (folio) management — all charges aggregated to a guest's folio
- Key card issuance tracking (conceptual — physical integration TBD)
- Night audit (end-of-day balancing and close)

### `reservations` Submodule
- Reservation creation (direct, phone, OTA) with source tracking
- Multi-room reservations and group bookings
- Rate plan management (Rack Rate, Corporate Rate, Advance Purchase)
- Cancellation policies and early departure handling
- Deposit collection and refunds via `atlashub-pay`
- No-show charging logic

### `housekeeping` Submodule
- Room status management (CLEAN, DIRTY, OUT_OF_ORDER, INSPECTED)
- Housekeeping task assignment (which room to clean next)
- Housekeeper app (mobile-first) — marks rooms clean, reports maintenance issues
- Laundry tracking and minibar restocking logs

### `maintenance` Submodule
- Maintenance request logging (from guests or housekeeping)
- Work order assignment to maintenance staff
- Priority-based scheduling
- OOO (Out of Order) room flagging that prevents reservations

### `revenue` Submodule
- Rate plan and rate code management
- Room type and rate grid (by date, day of week, season)
- Revenue per Available Room (RevPAR) tracking
- Forecasting inputs (occupancy projections)
- OTA channel management (Booking.com, Expedia) — future

### `billing` Submodule (Hotel-Specific)
- Guest folio — running bill for all charges during a stay
- Room charges posted nightly automatically
- F&B charges transferred from `atlashub-commerce` (restaurant orders added to room folio)
- Settlement methods: card, cash, bank transfer, corporate account
- Split folio — business travel personal charges vs. company charges on separate folios
- Tax handling: hotel levy, VAT on accommodation

### `conference` Submodule
- Conference room inventory and booking
- AV equipment and setup requirements
- Catering requests (linked to Commerce F&B)
- Event billing and deposit management

---

## 2. Domain Model

> [!NOTE]
> This section documents the **planned domain model** for reference and future development. No JPA entities or handlers exist yet.

### Core Aggregates

#### `Room` (Aggregate Root)
```
Room
├── id: Long
├── organizationId: Long
├── roomNumber: String
├── roomType: RoomType               ← STANDARD, DELUXE, SUITE, PRESIDENTIAL, etc.
├── floor: Integer
├── bedConfiguration: BedType        ← SINGLE, DOUBLE, TWIN, KING, QUEEN
├── maxOccupancy: Integer
├── baseRackRate: BigDecimal
├── currency: String
├── amenities: Set<String>
├── status: RoomStatus               ← VACANT_CLEAN, VACANT_DIRTY, OCCUPIED, OUT_OF_ORDER, OUT_OF_SERVICE
└── currentReservationId: Long       ← nullable
```

---

#### `GuestProfile` (Aggregate Root)
```
GuestProfile
├── id: Long
├── organizationId: Long             ← the hotel org
├── userId: Long                     ← nullable — if guest has an AtlasHub account
├── title: String
├── firstName: String
├── lastName: String
├── email: String                    ← EmailAddress value object
├── phone: String                    ← PhoneNumber value object
├── nationality: String
├── idType: GovernmentIdType         ← PASSPORT, NIN, DRIVERS_LICENSE, etc.
├── idNumber: String
├── dateOfBirth: LocalDate
├── loyaltyPoints: Integer           ← future
└── vipStatus: VipStatus             ← REGULAR, SILVER, GOLD, PLATINUM
```

---

#### `Reservation` (Aggregate Root)
```
Reservation
├── id: Long
├── organizationId: Long
├── confirmationNumber: String        ← e.g., "RES-2026-HOT-00842"
├── primaryGuestId: Long
├── guestIds: List<Long>              ← additional guests in group bookings
├── roomIds: List<Long>
├── ratePlanId: Long
├── checkInDate: LocalDate
├── checkOutDate: LocalDate
├── nights: Integer
├── adults: Integer
├── children: Integer
├── status: ReservationStatus         ← TENTATIVE, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW
├── source: ReservationSource         ← DIRECT, PHONE, BOOKING_COM, EXPEDIA, API
├── depositAmount: BigDecimal
├── depositChargeReference: String    ← nullable — pay module charge reference
├── cancellationPolicy: CancellationPolicy
├── notes: String
├── createdAt: ZonedDateTime
└── confirmedAt: ZonedDateTime
```

**Planned Business Methods:**
- `confirm()` — TENTATIVE → CONFIRMED; checks room availability guard
- `checkIn(roomId)` — CONFIRMED → CHECKED_IN; assigns room, opens `GuestFolio`
- `checkOut(settledBy)` — CHECKED_IN → CHECKED_OUT; triggers folio settlement via Pay
- `cancel(reason)` — CONFIRMED → CANCELLED; applies cancellation policy
- `markNoShow()` — CONFIRMED → NO_SHOW; releases room, applies no-show charge

---

#### `GuestFolio` (Aggregate Root)
```
GuestFolio
├── id: Long
├── reservationId: Long
├── guestId: Long
├── lines: List<FolioLine>            ← each charge added to the guest's bill
├── totalCharges: BigDecimal
├── totalCredits: BigDecimal
├── balance: BigDecimal
├── status: FolioStatus               ← OPEN, SETTLED, SPLIT, DISPUTED
└── settledAt: ZonedDateTime
```

---

#### `FolioLine` (Entity — child of GuestFolio)
```
FolioLine
├── id: Long
├── folioId: Long
├── date: LocalDate
├── description: String               ← "Room Charge", "Restaurant F&B", "Minibar", "Laundry"
├── amount: BigDecimal
├── type: FolioLineType               ← ROOM_CHARGE, FB_CHARGE, MINIBAR, EXTRA, TAX, DISCOUNT, PAYMENT
├── reference: String                 ← nullable — links to sales order in Commerce if F&B charge
└── postedAt: ZonedDateTime
```

---

#### `HousekeepingTask` (Aggregate Root)
```
HousekeepingTask
├── id: Long
├── organizationId: Long
├── roomId: Long
├── type: TaskType                    ← CHECKOUT_CLEANING, DAILY_SERVICING, DEEP_CLEAN, TURNDOWN
├── assignedTo: Long                  ← employeeId of housekeeper
├── status: TaskStatus                ← ASSIGNED, IN_PROGRESS, COMPLETED, INSPECTED, REJECTED
├── reportedIssues: List<String>
└── completedAt: ZonedDateTime
```

---

#### `MaintenanceRequest` (Aggregate Root)
```
MaintenanceRequest
├── id: Long
├── organizationId: Long
├── roomId: Long
├── reportedBy: Long                  ← guestId or employeeId
├── description: String
├── priority: MaintenancePriority     ← LOW, MEDIUM, HIGH, EMERGENCY
├── status: MaintenanceStatus         ← OPEN, IN_PROGRESS, COMPLETED
├── assignedTo: Long                  ← maintenance employee
└── resolvedAt: ZonedDateTime
```

---

#### `RatePlan` (Aggregate Root)
```
RatePlan
├── id: Long
├── organizationId: Long
├── name: String                      ← "Best Available Rate", "Corporate", "Advance Purchase"
├── code: String
├── mealPlan: MealPlan                ← ROOM_ONLY, BED_BREAKFAST, HALF_BOARD, FULL_BOARD, ALL_INCLUSIVE
├── isRefundable: Boolean
├── advancePurchaseDays: Integer      ← nullable — for advance purchase plans
├── roomRates: List<RoomTypeRate>     ← price per room type per date range
└── isActive: Boolean
```

---

#### `ConferenceRoom` (Aggregate Root)
```
ConferenceRoom
├── id: Long
├── organizationId: Long
├── name: String
├── capacity: Integer
├── layout: ConferenceLayout          ← CLASSROOM, BOARDROOM, THEATRE, U_SHAPE
├── rates: List<ConferenceRate>
├── amenities: Set<String>
└── status: RoomStatus
```

---

#### `ConferenceBooking` (Aggregate Root)
```
ConferenceBooking
├── id: Long
├── organizationId: Long
├── conferenceRoomId: Long
├── companyName: String
├── contactGuestId: Long
├── startDateTime: ZonedDateTime
├── endDateTime: ZonedDateTime
├── setup: ConferenceLayout
├── attendees: Integer
├── cateringRequired: Boolean
├── status: BookingStatus
└── depositAmount: BigDecimal
```

---

### Planned Value Objects

| Value Object | Values |
|---|---|
| `RoomType` | STANDARD, DELUXE, SUITE, PRESIDENTIAL |
| `BedType` | SINGLE, DOUBLE, TWIN, KING, QUEEN |
| `RoomStatus` | VACANT_CLEAN, VACANT_DIRTY, OCCUPIED, OUT_OF_ORDER, OUT_OF_SERVICE |
| `ReservationStatus` | TENTATIVE, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW |
| `ReservationSource` | DIRECT, PHONE, BOOKING_COM, EXPEDIA, API |
| `FolioStatus` | OPEN, SETTLED, SPLIT, DISPUTED |
| `FolioLineType` | ROOM_CHARGE, FB_CHARGE, MINIBAR, EXTRA, TAX, DISCOUNT, PAYMENT |
| `TaskType` | CHECKOUT_CLEANING, DAILY_SERVICING, DEEP_CLEAN, TURNDOWN |
| `TaskStatus` | ASSIGNED, IN_PROGRESS, COMPLETED, INSPECTED, REJECTED |
| `MaintenancePriority` | LOW, MEDIUM, HIGH, EMERGENCY |
| `MealPlan` | ROOM_ONLY, BED_BREAKFAST, HALF_BOARD, FULL_BOARD, ALL_INCLUSIVE |
| `VipStatus` | REGULAR, SILVER, GOLD, PLATINUM |
| `GovernmentIdType` | PASSPORT, NIN, DRIVERS_LICENSE, NATIONAL_ID |

---

## 3. Key Business Rules

- **Room Availability Guard**: No two confirmed reservations can overlap for the same room. Enforced via pessimistic lock on `Room` + date range during `reservation.confirm()`.
- **Night Audit**: At the end of each business day, the system automatically posts room charges to all open folios. The next day cannot begin in the PMS until the night audit is completed.
- **No-Show Handling**: If a guest with a CONFIRMED reservation does not check in by a configured cutoff time (e.g., 11 PM), the reservation transitions to `NO_SHOW`, the room is released, and no-show charges are applied per the rate plan policy.
- **OTA Parity**: If OTA channel management is active, rate changes must be pushed to all connected OTA channels simultaneously to maintain rate parity.
- **Split Folio**: A guest can request their F&B charges to be on a separate folio from their room charges — e.g., for business travel expense reporting.
- **Maker-Checker on Rate Changes**: Rate plan changes must be initiated by one staff member and approved by a revenue manager before taking effect. This will be enforced via `ratePlan.approve(approverId)` with the standard `approverId != initiatedBy` check.

---

## 4. Integration Points

| Integration | Module | How |
|---|---|---|
| Room charge accounting | `atlashub-accounting` | Nightly auto-post: Dr Accounts Receivable, Cr Room Revenue |
| F&B charges | `atlashub-commerce` | KOT / Sales Order linked to guest folio by room number |
| Deposit collection | `atlashub-pay` | `InitializeChargeCommand` with `ChargePurpose.HOTEL_DEPOSIT` |
| Folio settlement (checkout) | `atlashub-pay` | Charge total folio balance on departure |
| Housekeeping staff management | `atlashub-hr` | Housekeepers are `Employee` records; `HousekeepingTask.assignedTo` is an `employeeId` |
| Shuttle dispatch | `atlashub-logistics` | Airport / inter-property transfers are `Shipment` records |
| OTA channel sync | External — Booking.com API | `ChannelManagerAdapter` (future — not in scope) |

---

## 5. Planned Domain Events (Not Yet Implemented)

| Event | Published When | Intended Consumers |
|---|---|---|
| `ReservationConfirmedEvent` | Reservation confirmed | `notifications` (guest confirmation email), `atlashub-pay` (deposit charge) |
| `ReservationCancelledEvent` | Reservation cancelled | `notifications`, `atlashub-pay` (refund) |
| `GuestCheckedInEvent` | Guest checks in | `housekeeping` (room status → OCCUPIED), `notifications` |
| `GuestCheckedOutEvent` | Guest checks out | `accounting` (post revenue), `atlashub-pay` (folio settlement) |
| `RoomChargePostedEvent` | Nightly room charge posted | `accounting` (journal entry) |
| `MaintenanceRequestCreatedEvent` | Maintenance issue reported | `notifications` (alert maintenance team) |
| `HousekeepingTaskCompletedEvent` | Room cleaned | `frontdesk` (room status → VACANT_CLEAN) |
| `NoShowRecordedEvent` | Guest no-show | `atlashub-pay` (no-show charge), `notifications` |

---

## 6. Future Roadmap

- **OTA Channel Manager**: Two-way sync with Booking.com and Expedia (availability calendar, rate updates, reservation import via `ChannelManagerAdapter`)
- **Revenue Management Engine**: Dynamic pricing based on occupancy forecasts, competitor rates, and demand signals
- **Loyalty Program**: Guest points accumulation and redemption across all AtlasHub hotel properties
- **Mobile Key**: Guest uses phone as room key (requires hardware integration with door lock vendors)
- **Spa & Activities Booking**: Extend the reservation system to cover spa appointments and activity bookings
- **Multi-Property**: One organization managing multiple hotel properties — cross-property reservations, central housekeeping visibility, consolidated revenue reporting

---

## 7. Package Structure (Planned)

When development begins, this module will follow the canonical AtlasHub package structure:

```
atlashub-hotel/
└── src/main/java/com/atlashub/hotel/
    ├── frontdesk/
    │   ├── application/
    │   │   ├── commands/
    │   │   └── queries/
    │   ├── domain/
    │   │   ├── entities/    ← Room, GuestFolio, FolioLine
    │   │   ├── events/
    │   │   ├── exceptions/
    │   │   └── valueobject/
    │   ├── infrastructure/
    │   │   └── persistence/
    │   └── presentation/
    ├── reservations/
    │   ├── application/
    │   ├── domain/
    │   │   └── entities/    ← Reservation, GuestProfile
    │   ├── infrastructure/
    │   └── presentation/
    ├── housekeeping/
    │   ├── application/
    │   ├── domain/
    │   │   └── entities/    ← HousekeepingTask
    │   ├── infrastructure/
    │   └── presentation/
    ├── maintenance/
    │   ├── application/
    │   ├── domain/
    │   │   └── entities/    ← MaintenanceRequest
    │   ├── infrastructure/
    │   └── presentation/
    ├── revenue/
    │   ├── application/
    │   ├── domain/
    │   │   └── entities/    ← RatePlan, RoomTypeRate
    │   ├── infrastructure/
    │   └── presentation/
    ├── billing/
    │   ├── application/
    │   ├── domain/
    │   └── infrastructure/
    └── conference/
        ├── application/
        ├── domain/
        │   └── entities/    ← ConferenceRoom, ConferenceBooking
        ├── infrastructure/
        └── presentation/
```

> [!IMPORTANT]
> The full application layer (commands, queries, handlers), infrastructure layer (JPA entities, mappers, repositories, adapters, listeners), and presentation layer (controllers, DTOs, RBAC table, WebSocket events) will be documented in this file when this module enters active development.
