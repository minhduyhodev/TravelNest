import { Navigate, Outlet, useLocation } from "react-router-dom";

import { ROUTES } from "@/routes/paths";
import { useAuthStore } from "@/stores/useAuthStore";

export function ProtectedRoute({ allowedRoles = [] }) {
  const location = useLocation();
  const user = useAuthStore((state) => state.user);

  if (!user) {
    return <Navigate to={ROUTES.login} replace state={{ from: location }} />;
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    return <Navigate to={ROUTES.account} replace />;
  }

  return <Outlet />;
}
