import { create } from "zustand";

export const useAuthStore = create((set) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  setSession: (session) => set(session),
  clearSession: () => set({ accessToken: null, refreshToken: null, user: null })
}));
