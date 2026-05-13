import { create } from "zustand";

export const useBookingStore = create((set) => ({
  draft: {},
  setDraft: (payload) =>
    set((state) => ({
      draft: {
        ...state.draft,
        ...payload
      }
    })),
  resetDraft: () => set({ draft: {} })
}));
