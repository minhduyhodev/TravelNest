import { create } from "zustand";
import { persist } from "zustand/middleware";

export const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      hasHydrated: false,
      setHasHydrated: (value) => set(() => ({ hasHydrated: value })),
      setSession: (session) =>
        set(() => ({
          accessToken: session.accessToken ?? null,
          refreshToken: session.refreshToken ?? null,
          user: session.user ?? null
        })),
      clearSession: () =>
        set(() => ({ accessToken: null, refreshToken: null, user: null }))
    }),
    {
      name: "travelnest-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user
      }),
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated?.(true);
      }
    }
  )
);
