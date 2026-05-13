import { Outlet } from "react-router-dom";

import { DashboardShell } from "@/components/layout/DashboardShell";

export function StaffDashboardLayout() {
  return (
    <DashboardShell
      variant="staff"
      title="Staff operations"
      subtitle="Booking queue, schedule handling, and status updates."
    >
      <Outlet />
    </DashboardShell>
  );
}
