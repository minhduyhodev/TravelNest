import { create } from "zustand";
import { persist } from "zustand/middleware";

export const useAuthStore = create(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setSession: (session) => set(session),
      clearSession: () => set({ accessToken: null, refreshToken: null, user: null })
    }),
    {
      name: "travelnest-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user
      })
    }
  )
);
