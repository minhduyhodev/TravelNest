# agent.md

## Mission

Build TravelNest backend as a production-oriented Spring Boot API that supports travel discovery, booking, payment, RBAC, and operational dashboards with a clean service boundary for the React SPA frontend.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- MySQL
- Redis
- Spring Mail
- springdoc OpenAPI

## Architecture rules

- Follow a layered backend structure: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `config`, `security`, `exception`.
- Controllers expose REST endpoints and stay thin.
- Services own business rules, workflow orchestration, and transaction boundaries.
- Repositories own persistence access only.
- Entities model persistence, not API contracts.
- DTOs are regular classes used for request and response transport.
- Controllers must not return JPA entities directly.

## DTO and mapping rules

- Use request DTOs for create, update, filter, and auth input payloads.
- Use response DTOs for all external API responses.
- Put validation annotations on request DTOs where possible.
- Keep entity-to-DTO mapping explicit and readable.
- Never expose internal fields, lazy relationships, passwords, tokens, or audit internals by returning entities directly.

## Domain boundaries

- Auth and user management
- Hotel, room, amenity, and inventory management
- Tour and restaurant modules when implemented
- Search and filtering
- Booking flow and availability checks
- Payment flow including VNPay and MoMo integration points
- Voucher and promotion logic
- Review and rating logic
- CMS, admin, staff operations, and reporting

## Business logic rules

- Put booking rules in services, not controllers.
- Guard against overbooking, invalid state transitions, stale payment callbacks, and voucher misuse.
- Be explicit about status transitions for booking, payment, refund, and review visibility.
- Use transactions for multi-step persistence changes that must succeed together.
- Keep side effects such as email, cache invalidation, and payment confirmation coordinated and traceable.

## Security rules

- RBAC must be enforced in security configuration and, when needed, at service boundaries.
- Never trust role or user identity sent from the client without server-side verification.
- Be strict with auth token parsing, current-user resolution, and protected endpoints.
- Sanitize error exposure for auth, payment, and admin operations.

## Data and persistence rules

- Model JPA relationships deliberately; avoid accidental eager loading chains.
- Be careful with cascade behavior, orphan removal, and fetch strategy.
- Repositories should return only what the use case needs.
- Redis should be used for caching, temporary tokens, rate-sensitive flows, or short-lived booking-related state when appropriate.

## API conventions

- Keep endpoint naming resource-oriented and predictable.
- Standardize response format, validation error shape, and exception handling.
- Document APIs with OpenAPI annotations or conventions that keep Swagger useful.
- Keep frontend-facing field names consistent with the React SPA contract.

## Testing and verification

- Run `./mvnw test` or `mvnw.cmd test` in `backend/` for backend changes.
- For compile-level verification, run `mvnw.cmd -q -DskipTests compile` when needed.
- Add tests for service logic, validation, and security-sensitive behavior when you change them.
- Verify negative cases too: unauthorized access, invalid booking input, expired voucher, duplicate payment callback, and missing resources.

## Naming and code style

- Prefer clear names such as `BookingService`, `CreateBookingRequestDto`, `BookingResponseDto`, `PaymentCallbackController`.
- Keep controllers, services, and DTOs focused on one business area.
- Avoid god services and catch-all utility classes.
- Comments should explain business intent or non-obvious constraints, not restate code.

## Definition of done

- The API contract is DTO-based and does not leak entities.
- Validation, error handling, and role protection are covered.
- Transactional business flows are consistent and safe.
- The code compiles and relevant tests pass.
- The change fits the TravelNest domain and keeps the backend ready for frontend integration.
