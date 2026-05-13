# agent.md

## Mission

Build TravelNest frontend as a production-style React SPA focused on booking flow, dashboard usability, and clean integration with the Spring Boot backend.

## Stack

- Vite
- React 18
- JavaScript + JSX
- Tailwind CSS
- React Router DOM
- Zustand
- TanStack Query
- React Hook Form + Zod
- Axios
- react-i18next

## Architecture rules

- Keep the frontend in a layered SPA structure: `routes`, `pages`, `layouts`, `components`, `api`, `services`, `stores`, `utils`, `i18n`.
- Do not reintroduce Next.js concepts such as App Router, server components, SSR, ISR, SSG, or middleware-based routing.
- Keep route files thin. Routing owns navigation and guards, not business UI.
- Keep pages thin. Pages compose sections and containers; they should not become a dumping ground for logic.
- Shared primitives belong in `components/ui`.
- Shared layout shells belong in `components/layout`.
- Shared forms belong in `components/forms`.
- Reusable business cards, summaries, and dashboards belong in `components/data-display`.

## State and data boundaries

- Use TanStack Query for server state, cache, refetching, and API synchronization.
- Use Zustand only for client-side app state such as auth session, booking draft, filters, and shell state.
- Do not duplicate API data in Zustand unless there is a clear offline or multi-step workflow reason.
- Centralize backend calls in `src/api` and `src/services/http`.
- Keep endpoint contracts aligned with Spring Boot DTOs and response wrappers.

## Routing boundaries

- Public area: landing, search, service detail, checkout entry.
- Auth area: login, register, forgot/reset password when added.
- User area: profile, bookings, payments, reviews, saved services.
- Staff area: booking operations, approval queue, support handling.
- Admin area: users, inventory, CMS, reports, analytics, configuration.
- Role protection belongs in `src/routes/ProtectedRoute.jsx`.

## UI quality bar

- Mobile-first by default.
- Use reusable sections and cards before creating one-off page markup.
- Prefer consistent spacing, typography, and token usage over ad-hoc inline styling.
- Build for real booking flows: filters, availability, pricing, checkout, confirmation, dashboards.
- Keep empty, loading, error, and skeleton states in mind for every user-facing async page.

## Forms and validation

- Form input validation belongs in Zod schemas.
- React Hook Form owns field registration, submission state, and field error display.
- Never trust only frontend validation for business rules; frontend validates UX, backend validates truth.

## Naming and code style

- Use `.jsx` for React components and `.js` for utilities, stores, services, constants, and API modules.
- Prefer explicit names such as `BookingSummaryCard`, `UserDashboardPage`, `useAuthStore`.
- Keep imports stable and shallow through aliases such as `@/components/...` and `@/routes/...`.
- Avoid mixing multiple architectural styles in the same tree.

## Backend integration rules

- Do not assume frontend field names if the backend contract is unclear; verify against DTOs and API docs.
- Handle auth tokens through the shared Axios client and auth store.
- Normalize API error handling in one place before surfacing messages to UI components.
- Be careful with currency, locale, date range, booking status, payment status, and role names because these affect business behavior.

## Definition of done

- `npm run build` passes in `frontend/`.
- New routes are wired into the router and protected correctly if required.
- Loading, empty, and error states exist for async screens.
- Shared components are reused where appropriate instead of duplicating UI.
- No Next.js terminology or architecture leaks back into the SPA.
