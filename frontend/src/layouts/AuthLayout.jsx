import { Navigate, Outlet } from "react-router-dom";

import { PageShell } from "@/components/layout/PageShell";
import { useAuthStore } from "@/stores/useAuthStore";
import { getDefaultRouteByRole } from "@/utils/auth";

export function AuthLayout() {
  const user = useAuthStore((state) => state.user);
  const hasHydrated = useAuthStore((state) => state.hasHydrated);

  if (!hasHydrated) {
    return null;
  }

  if (user) {
    return <Navigate to={getDefaultRouteByRole(user.role)} replace />;
  }

  return (
    <PageShell className="flex min-h-screen items-center justify-center py-12">
      <div className="w-full max-w-md">
        <Outlet />
      </div>
    </PageShell>
  );
}
