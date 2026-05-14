import { NavLink } from "react-router-dom";

import { cn } from "@/utils/cn";

const navigationMap = {
  user: [
    { label: "Overview", href: "/account" },
    { label: "Profile", href: "/account/profile" },
    { label: "My bookings", href: "/account/bookings" }
  ],
  staff: [
    { label: "Operations", href: "/staff" },
    { label: "Booking queue", href: "/staff/queue" }
  ],
  admin: [
    { label: "Overview", href: "/admin" },
    { label: "Users", href: "/admin/users" },
    { label: "Reports", href: "/admin/reports" }
  ]
};

export function DashboardShell({ variant, title, subtitle, children }) {
  const items = navigationMap[variant];

  return (
    <div className="min-h-screen bg-surface-1">
      <div className="container grid gap-6 py-6 lg:grid-cols-[260px_1fr]">
        <aside className="rounded-xl border bg-card p-4">
          <p className="mb-4 font-heading text-lg font-semibold capitalize">{variant}</p>
          <nav className="space-y-2">
            {items.map((item) => (
              <NavLink
                key={item.href}
                to={item.href}
                className={({ isActive }) =>
                  cn(
                    "block rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:bg-accent hover:text-foreground",
                    isActive && "bg-accent text-foreground"
                  )
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <section className="space-y-6">
          <header className="rounded-xl border bg-card p-6">
            <h1 className="text-2xl font-semibold">{title}</h1>
            <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>
          </header>
          {children}
        </section>
      </div>
    </div>
  );
}
