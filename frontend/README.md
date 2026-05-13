# TravelNest Frontend

Production-style React SPA foundation for TravelNest.

## Stack

- Vite
- React 18
- JavaScript + JSX
- Tailwind CSS
- React Router DOM
- Zustand
- TanStack Query
- React Hook Form
- Zod
- react-i18next
- Axios

## Architecture

```text
src/
  app/
    providers/
  api/
  routes/
  layouts/
  pages/
    public/
    auth/
    account/
    staff/
    admin/
  components/
    ui/
    layout/
    forms/
    feedback/
    data-display/
  services/
    http/
    endpoints/
  stores/
  i18n/
  utils/
    validation/
  locales/
    vi/
    en/
  styles/
```

## Areas covered

- Public pages
- Auth pages
- User dashboard
- Staff dashboard
- Admin dashboard

## Commands

```bash
npm install
cp .env.example .env.local
npm run dev
```

## Notes

- This is a CSR-only app.
- Spring Boot API integration is prepared through Axios and endpoint maps.
- Query caching is handled by TanStack Query.
- Client session and booking draft state are handled with Zustand.
- Routing is organized in `src/routes` with role-based guards for user, staff, and admin areas.
