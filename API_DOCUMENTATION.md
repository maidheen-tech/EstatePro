# EstatePro API

Base path: `/api`. JSON errors include `timestamp`, `status`, and `error`; validation errors include a `fields` object. Protected endpoints require `Authorization: Bearer <JWT>`. OpenAPI UI: `/swagger-ui.html`.

## Authentication

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/auth/register` | Public | Create BUYER or SELLER only |
| POST | `/auth/login` | Public | Validate credentials; return JWT and profile |
| GET | `/auth/me` | Authenticated | Read signed-in profile |

Registration payload: `{"fullName":"A. Customer","email":"a@example.test","password":"at-least-10-chars","role":"BUYER"}`.

## Property marketplace

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/properties` | Public | Search, sort and page published listings |
| GET | `/properties/{id}` | Public for published; owner/agent/admin for own unpublished | Property details |
| GET | `/properties/mine` | Authenticated | Listings managed by the caller |
| POST | `/properties` | Seller, agent, admin | Submit listing for review |
| PUT | `/properties/{id}` | Owner, assigned agent, admin | Edit; non-admin edits return to pending |
| DELETE | `/properties/{id}` | Owner, assigned agent, admin | Archive listing (soft delete) |
| PATCH | `/admin/properties/{id}/moderation` | Admin | Publish or reject with reason |
| PUT | `/admin/properties/{id}/agent/{agentId}` | Admin | Assign an existing agent |

`GET /properties` query parameters: `q`, `city`, `type`, `listing`, `minPrice`, `maxPrice`, `bedrooms`, `page`, `size` (1–48), and `sort` (`latest`, `price-low`, `price-high`, `area`). Response is Spring's page shape with `content`, `totalElements`, and `totalPages`.

Property submission fields: `title`, `description`, `propertyType`, `listingType`, `city`, `locality`, `address`, `price`, `bedrooms`, `bathrooms`, `areaSqft`, `imageUrl`. Images must be selected from a local `/assets/images/` path; arbitrary remote image paths are rejected.

## User workflows

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET / POST / DELETE | `/favorites[/{propertyId}]` | Buyer | Persist/remove saved properties |
| GET / POST / PATCH | `/enquiries[/{id}]` | Buyer submits; assigned agent/admin updates | Create and manage lead status |
| GET / POST / PATCH | `/appointments[/{id}]` | Buyer requests; assigned agent/admin confirms | Request and manage visits |
| GET / POST / PATCH | `/bookings[/{propertyId or id}]` | Buyer requests/cancels; assigned agent/admin updates | Booking requests and status |
| GET | `/payments` | Buyer | View own simulated payment history |
| POST | `/payments/{bookingId}/demo` | Buyer, own confirmed booking | Record a clearly labeled DEMO transaction |
| GET | `/recommendations` | Authenticated | Rule-based matching by `city`, `type`, `maxPrice`, `bedrooms` |
| GET | `/dashboard` | Authenticated | Database-backed role-specific workspace data |
| POST | `/contact` | Public | Validate and store a general contact message |
| GET | `/agents` | Public | Return enabled agent names and local portrait paths (no private account data) |

Visit request payload: `{"propertyId":"...","date":"YYYY-MM-DD","time":"HH:mm"}`. Choose a future date and a 30-minute slot between 09:00 and 17:30. An agent's slot is protected by both application validation and a database unique constraint.

Lead statuses: `NEW`, `CONTACTED`, `INTERESTED`, `VISIT_SCHEDULED`, `NEGOTIATION`, `CONVERTED`, `LOST`.

## Administration

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/admin/users?q=&role=` | Admin | Search accounts |
| PATCH | `/admin/users/{id}` | Admin | Set role and enabled status |
| GET | `/admin/reports/properties.csv` | Admin | Export listings |

## Important current boundaries

The current service does not yet expose upload endpoints, password-reset/verification flows, notifications, complaints, reviews, audit-log history, saved comparison persistence, appointment rescheduling, payment refunds, or historical analytics. File upload is deliberately not simulated by accepting arbitrary URLs. The role and record ownership checks are enforced server-side for implemented workflows.
