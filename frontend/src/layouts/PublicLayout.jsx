import { Outlet } from "react-router-dom";

import { PublicFooter } from "@/components/layout/PublicFooter";
import { PublicHeader } from "@/components/layout/PublicHeader";

export function PublicLayout() {
  return (
    <div className="min-h-screen">
      <PublicHeader />
      <Outlet />
      <PublicFooter />
    </div>
  );
}
