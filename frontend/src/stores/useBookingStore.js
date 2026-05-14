import { create } from "zustand";
import { persist } from "zustand/middleware";

export const useBookingStore = create(
  persist(
    (set) => ({
      draft: {},
      setDraft: (payload) =>
        set((state) => ({
          draft: {
            ...state.draft,
            ...payload
          }
        })),
      replaceDraft: (payload) => set({ draft: payload || {} }),
      resetDraft: () => set({ draft: {} })
    }),
    {
      name: "travelnest-booking-draft"
    }
  )
);
