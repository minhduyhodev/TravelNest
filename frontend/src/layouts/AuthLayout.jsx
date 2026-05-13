import { Outlet } from "react-router-dom";

import { PageShell } from "@/components/layout/PageShell";

export function AuthLayout() {
  return (
    <PageShell className="flex min-h-screen items-center justify-center py-12">
      <div className="w-full max-w-md">
        <Outlet />
      </div>
    </PageShell>
  );
}
