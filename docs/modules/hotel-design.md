# Atlas Hotel — Future Vision (`atlashub-hotel`)

> **Status: NOT IN MVP.** This document describes the full architectural vision for the Hotel PMS (Property Management System) module. No code will be written for this module in the current development cycle. This document exists so that when development begins, the domain has been thought through correctly and is not designed retroactively.
>
> **Restaurant/hospitality POS (table management, kitchen order tickets) is part of Atlas Commerce** and IS in the MVP.

---

## Role & Purpose

Atlas Hotel is a full **Hotel Property Management System (PMS)** designed for African hotels — from boutique guesthouses to large resort chains. It integrates natively with all other AtlasHub modules:
- `atlashub-pay` handles all financial transactions (deposits, room charges, refunds)
- `atlashub-commerce` handles the hotel's restaurant and bar operations (table management, KOTs, F&B billing)
- `atlashub-accounting` receives all hotel revenue journal entries automatically
- `atlashub-hr` manages hotel staff (front desk, housekeeping, F&B)
- `atlashub-logistics` can manage airport shuttle and inter-property vehicle dispatch

The competitive advantage over existing African hotel PMS solutions (Opera, Protel) is deep integration with a payment and business management platform — no more separate POS system, no separate accounting software, no separate HR tool.

---

## 1. Core Submodules

### `frontdesk`
The central operational hub of the hotel.
- Walk-in and advance reservations
- Guest check-in and check-out
- Room assignment and room moves
- Bill (folio) management — all charges aggregated to a guest's folio
- Key card issuance tracking (conceptual — physical integration TBD)
- Night audit (end-of-day balancing and close)

### `reservations`
- Reservation creation (direct, phone, OTA) with source tracking
- Multi-room reservations and group bookings
- Rate plan management (Rack Rate, Corporate Rate, Advance Purchase)
- Cancellation policies and early departure handling
- Deposit collection and refunds via Pay
- No-show charging logic

### `housekeeping`
- Room status management (CLEAN, DIRTY, OUT_OF_ORDER, INSPECTED)
- Housekeeping task assignment (which room to clean next)
- Housekeeper app (mobile-first) — marks rooms clean, reports maintenance issues
- Laundry tracking
- Minibar restocking logs

### `maintenance`
- Maintenance request logging (from guests or housekeeping)
- Work order assignment to maintenance staff
- Priority-based scheduling
- OOO (Out of Order) room flagging that prevents reservations

### `revenue`
- Rate plan and rate code management
- Room type and rate grid (by date, day of week, season)
- Revenue per Available Room (RevPAR) tracking
- Forecasting inputs (occupancy projections)
- OTA channel management (Booking.com, Expedia) — future

### `billing` (Hotel-specific)
- Guest folio (running bill for all charges during a stay)
- Room charges posted nightly automatically
- F&B charges transferred from Commerce (restaurant orders added to room folio)
- Settlement methods: card, cash, bank transfer, corporate account
- Split folio (business travel — personal charges vs company charges on separate folios)
- Tax handling: hotel levy, VAT on accommodation

### `conference`
- Conference room inventory and booking
- AV equipment and setup requirements
- Catering requests (linked to Commerce F&B)
- Event billing and deposit management

---

## 2. Domain Model (Future — For Reference)

### Core Aggregates

**`Room` (Aggregate Root)**
```
Room
├── id: Long
├── organizationId: Long
├── roomNumber: String
├── roomType: RoomType               ← STANDARD, DELUXE, SUITE, PRESIDENTIAL, etc.
├── floor: Integer
├── bedConfiguration: BedType        ← SINGLE, DOUBLE, TWIN, KING, QUEEN
├── maxOccupancy: Integer
├── baseRackRate: Money
├── amenities: Set<String>
├── status: RoomStatus               ← VACANT_CLEAN, VACANT_DIRTY, OCCUPIED, OUT_OF_ORDER, OUT_OF_SERVICE
└── currentReservationId: Long       ← nullable
```

**`GuestProfile` (Aggregate Root)**
```
GuestProfile
├── id: Long
├── organizationId: Long             ← the hotel org
├── userId: Long                     ← nullable — if guest has AtlasHub account
├── title: String
├── firstName: String
├── lastName: String
├── email: EmailAddress
├── phone: PhoneNumber
├── nationality: String
├── idType: GovernmentIdType
├── idNumber: String
├── dateOfBirth: LocalDate
├── loyaltyPoints: Integer           ← future
└── vipStatus: VipStatus             ← REGULAR, SILVER, GOLD, PLATINUM
```

**`Reservation` (Aggregate Root)**
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
├── depositAmount: Money
├── depositChargeReference: String    ← nullable — pay module reference
├── cancellationPolicy: CancellationPolicy
├── notes: String
├── createdAt: ZonedDateTime
└── confirmedAt: ZonedDateTime
```

**`GuestFolio` (Aggregate Root)**
```
GuestFolio
├── id: Long
├── reservationId: Long
├── guestId: Long
├── lines: List<FolioLine>            ← each charge added to the guest's bill
├── totalCharges: Money
├── totalCredits: Money
├── balance: Money
├── status: FolioStatus               ← OPEN, SETTLED, SPLIT, DISPUTED
└── settledAt: ZonedDateTime
```

**`FolioLine` (Entity)**
```
FolioLine
├── id: Long
├── folioId: Long
├── date: LocalDate
├── description: String               ← "Room Charge", "Restaurant F&B", "Minibar", "Laundry"
├── amount: Money
├── type: FolioLineType               ← ROOM_CHARGE, FB_CHARGE, MINIBAR, EXTRA, TAX, DISCOUNT, PAYMENT
├── reference: String                 ← nullable — links to sales order in Commerce if F&B charge
└── postedAt: ZonedDateTime
```

**`HousekeepingTask` (Aggregate Root)**
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

**`MaintenanceRequest` (Aggregate Root)**
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

**`RatePlan` (Aggregate Root)**
```
RatePlan
├── id: Long
├── organizationId: Long
├── name: String                      ← "Best Available Rate", "Corporate", "Advance Purchase"
├── code: String
├── rateMeals: MealPlan               ← ROOM_ONLY, BED_BREAKFAST, HALF_BOARD, FULL_BOARD, ALL_INCLUSIVE
├── isRefundable: Boolean
├── advancePurchaseDays: Integer      ← nullable — for advance purchase plans
├── roomRates: List<RoomTypeRate>     ← price per room type per date range
└── isActive: Boolean
```

**`ConferenceRoom` (Aggregate Root)**: `id`, `organizationId`, `name`, `capacity`, `layout` (CLASSROOM, BOARDROOM, THEATRE, U_SHAPE), `rates: List<ConferenceRate>`, `amenities: Set<String>`, `status`

**`ConferenceBooking` (Aggregate Root)**: `id`, `organizationId`, `conferenceRoomId`, `companyName`, `contactGuestId`, `startDateTime`, `endDateTime`, `setup: ConferenceLayout`, `attendees: Integer`, `cateringRequired: Boolean`, `status`, `depositAmount`

---

## 3. Key Business Rules

- **Room Availability Guard**: No two confirmed reservations can overlap for the same room. Checked via pessimistic lock on room + date range during reservation confirmation.
- **Night Audit**: At the end of each day, the system automatically posts room charges to all open folios for that night. Must be run before the next day can begin in the PMS.
- **No-Show Handling**: If a guest with a confirmed reservation does not check in by a configured cutoff time (e.g., 11 PM), the reservation transitions to NO_SHOW and the room is released. No-show charges are applied per the rate plan policy.
- **OTA Parity**: If OTA channel management is active, rate changes must be pushed to all connected OTA channels simultaneously to maintain rate parity.
- **Split Folio**: A guest can request their F&B charges to be on a separate folio from their room charges — e.g., for business travel expense reporting.

---

## 4. Integration Points

| Integration | Module | How |
|---|---|---|
| Room charges | `atlashub-accounting` | Nightly auto-post: Dr Accounts Receivable, Cr Room Revenue |
| F&B charges | `atlashub-commerce` | KOT/Sales Order linked to guest folio by room number |
| Deposit collection | `atlashub-pay` | `InitializeChargeUseCase` with `ChargePurpose.HOTEL_DEPOSIT` |
| Guest checkout (full settlement) | `atlashub-pay` | Charge total folio balance on departure |
| Housekeeping staff management | `atlashub-hr` | Housekeepers are `Employee` records |
| Shuttle dispatch | `atlashub-logistics` | Airport/inter-property transfers are `Shipment` records |
| OTA channel sync | External — Booking.com API | `ChannelManagerAdapter` (future) |

---

## 5. Future Roadmap

- **OTA Channel Manager**: Two-way sync with Booking.com and Expedia (availability calendar, rate updates, reservation import)
- **Revenue Management Engine**: Dynamic pricing based on occupancy forecasts, competitor rates, demand signals
- **Loyalty Program**: Guest points accumulation and redemption across all AtlasHub hotel properties
- **Mobile Key**: Guest uses phone as room key (requires hardware integration)
- **Spa & Activities Booking**: Extend reservation system to cover spa appointments and activity bookings
- **Multi-Property**: One organization managing multiple hotel properties — cross-property reservations, central housekeeping visibility
