import { Menu, PlaneTakeoff } from "lucide-react";
import { NavLink, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Button } from "@/components/ui/button";
import { ROUTES } from "@/routes/paths";
import { useAuthStore } from "@/stores/useAuthStore";
import { getDefaultRouteByRole } from "@/utils/auth";

export function PublicHeader() {
  const { t } = useTranslation("common");
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const clearSession = useAuthStore((state) => state.clearSession);
  const hasHydrated = useAuthStore((state) => state.hasHydrated);

  const handleLogout = () => {
    clearSession();
    navigate(ROUTES.home, { replace: true });
  };

  return (
    <header className="sticky top-0 z-40 border-b bg-background/90 backdrop-blur">
      <div className="container flex h-16 items-center justify-between gap-4">
        <NavLink to={ROUTES.home} className="flex items-center gap-2 text-primary">
          <PlaneTakeoff className="h-5 w-5" />
          <span className="font-heading text-lg font-semibold">{t("brand")}</span>
        </NavLink>
        <nav className="hidden items-center gap-5 md:flex">
          <NavLink className="text-sm text-muted-foreground hover:text-foreground" to={ROUTES.hotels}>
            {t("hotels")}
          </NavLink>
          <NavLink className="text-sm text-muted-foreground hover:text-foreground" to={ROUTES.tours}>
            {t("tours")}
          </NavLink>
          <NavLink className="text-sm text-muted-foreground hover:text-foreground" to={ROUTES.restaurants}>
            {t("restaurants")}
          </NavLink>
        </nav>
        <div className="flex items-center gap-2">
          {hasHydrated && user ? (
            <>
              <div className="hidden text-right sm:block">
                <p className="text-sm font-medium text-foreground">{user.fullName}</p>
                <p className="text-xs text-muted-foreground">{user.role}</p>
              </div>
              <Button variant="ghost" asChild>
                <NavLink to={getDefaultRouteByRole(user.role)}>{t("dashboard")}</NavLink>
              </Button>
              <Button variant="outline" onClick={handleLogout}>
                Logout
              </Button>
            </>
          ) : (
            <>
              <Button variant="ghost" asChild>
                <NavLink to={ROUTES.login}>{t("login")}</NavLink>
              </Button>
              <Button asChild>
                <NavLink to={ROUTES.register}>{t("register")}</NavLink>
              </Button>
            </>
          )}
          <button className="rounded-md p-2 md:hidden">
            <Menu className="h-5 w-5" />
          </button>
        </div>
      </div>
    </header>
  );
}
