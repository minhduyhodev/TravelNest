import { useEffect } from "react";
import { useQuery } from "@tanstack/react-query";

import { fetchMe } from "@/api/auth";
import { queryKeys } from "@/api/queryKeys";
import { useAuthStore } from "@/stores/useAuthStore";

export function AuthBootstrap({ children }) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const refreshToken = useAuthStore((state) => state.refreshToken);
  const user = useAuthStore((state) => state.user);
  const hasHydrated = useAuthStore((state) => state.hasHydrated);
  const setSession = useAuthStore((state) => state.setSession);
  const clearSession = useAuthStore((state) => state.clearSession);

  const meQuery = useQuery({
    queryKey: queryKeys.auth.me,
    queryFn: fetchMe,
    enabled: hasHydrated && Boolean(accessToken) && !user,
    retry: false
  });

  useEffect(() => {
    if (meQuery.data) {
      setSession({
        accessToken,
        refreshToken,
        user: meQuery.data
      });
    }
  }, [accessToken, meQuery.data, refreshToken, setSession]);

  useEffect(() => {
    if (hasHydrated && !accessToken && user) {
      clearSession();
    }
  }, [accessToken, clearSession, hasHydrated, user]);

  if (!hasHydrated) {
    return null;
  }

  return children;
}
