import { Outlet } from "react-router-dom";

import { DashboardShell } from "@/components/layout/DashboardShell";

export function AdminDashboardLayout() {
  return (
    <DashboardShell
      variant="admin"
      title="Admin control center"
      subtitle="Platform metrics, users, and service management."
    >
      <Outlet />
    </DashboardShell>
  );
}
