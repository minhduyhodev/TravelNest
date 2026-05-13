import { create } from "zustand";

export const useShellStore = create((set) => ({
  mobileMenuOpen: false,
  sidebarOpen: true,
  setMobileMenuOpen: (mobileMenuOpen) => set({ mobileMenuOpen }),
  setSidebarOpen: (sidebarOpen) => set({ sidebarOpen })
}));
