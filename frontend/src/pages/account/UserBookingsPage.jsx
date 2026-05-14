import { NavLink } from "react-router-dom";

import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getServiceTypeLabel } from "@/features/booking/draft";
import { useBookingStore } from "@/stores/useBookingStore";

export function UserBookingsPage() {
  const draft = useBookingStore((state) => state.draft);

  return (
    <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
      <Card>
        <CardHeader>
          <CardTitle>Booking history</CardTitle>
          <CardDescription>
            Backend history endpoints are the next Phase 3 step. Until then, this area keeps the current draft visible
            and ready for API integration.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {draft.serviceType ? (
            <div className="rounded-xl border bg-surface-1 p-4">
              <p className="text-sm font-medium">
                Active draft: {draft.serviceName} ({getServiceTypeLabel(draft.serviceType)})
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                Continue editing this draft in checkout, then wire it to order and booking APIs next.
              </p>
              <div className="mt-4">
                <Button asChild>
                  <NavLink to="/checkout">Open checkout draft</NavLink>
                </Button>
              </div>
            </div>
          ) : (
            <div className="rounded-xl border border-dashed p-4 text-sm text-muted-foreground">
              No booking draft yet. Start from a hotel, tour, or restaurant detail page to prepare your first order.
            </div>
          )}
          <div className="rounded-xl border p-4 text-sm text-muted-foreground">
            Planned next step: replace this shell with server-backed booking history, filters, and detail timeline once
            Phase 3 backend endpoints are in place.
          </div>
        </CardContent>
      </Card>
      <BookingSummaryCard />
    </div>
  );
}
