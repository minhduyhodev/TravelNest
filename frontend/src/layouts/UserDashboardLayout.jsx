import { Outlet } from "react-router-dom";

import { DashboardShell } from "@/components/layout/DashboardShell";

export function UserDashboardLayout() {
  return (
    <DashboardShell
      variant="user"
      title="My account"
      subtitle="Bookings, personal profile, and saved travel plans."
    >
      <Outlet />
    </DashboardShell>
  );
}
