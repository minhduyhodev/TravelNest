import { useQuery } from "@tanstack/react-query";
import { CalendarDays, Clock3, MapPinned, ReceiptText } from "lucide-react";
import { NavLink } from "react-router-dom";

import { fetchMyOrderHistory } from "@/api/orders";
import { queryKeys } from "@/api/queryKeys";
import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { PageSkeleton } from "@/components/feedback/PageSkeleton";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getServiceTypeLabel } from "@/features/booking/draft";
import { useBookingStore } from "@/stores/useBookingStore";
import { formatCurrency } from "@/utils/currency";

function formatOrderMoment(order) {
  const values = [
    order.startDate,
    order.endDate && order.endDate !== order.startDate ? `to ${order.endDate}` : null
  ]
    .filter(Boolean)
    .join(" ");

  if (order.serviceTime) {
    return values ? `${values} at ${order.serviceTime}` : order.serviceTime;
  }

  return values || "Schedule will be confirmed";
}

export function UserBookingsPage() {
  const draft = useBookingStore((state) => state.draft);
  const historyQuery = useQuery({
    queryKey: queryKeys.booking.history,
    queryFn: fetchMyOrderHistory
  });

  if (historyQuery.isLoading) {
    return <PageSkeleton />;
  }

  return (
    <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
      <Card>
        <CardHeader>
          <CardTitle>Booking history</CardTitle>
          <CardDescription>
            Review your saved checkout drafts and the service schedule captured for each order.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {draft.serviceType ? (
            <div className="rounded-xl border bg-surface-1 p-4">
              <p className="text-sm font-medium">
                Active draft: {draft.serviceName} ({getServiceTypeLabel(draft.serviceType)})
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                Continue editing this draft in checkout or save it to your order history.
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

          {historyQuery.isError ? (
            <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
              {historyQuery.error.message}
            </div>
          ) : null}

          {!historyQuery.isError && historyQuery.data?.length ? (
            <div className="space-y-4">
              {historyQuery.data.map((order) => (
                <div key={order.id} className="rounded-xl border p-4">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="space-y-1">
                      <p className="text-base font-semibold">{order.serviceName}</p>
                      <p className="text-sm text-muted-foreground">
                        {getServiceTypeLabel(order.serviceType)} - {order.orderCode}
                      </p>
                    </div>
                    <div className="rounded-full bg-accent px-3 py-1 text-xs font-medium text-primary">
                      {order.status}
                    </div>
                  </div>
                  <div className="mt-4 grid gap-3 md:grid-cols-2">
                    <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
                      <CalendarDays className="mt-0.5 h-4 w-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium">Schedule</p>
                        <p className="text-sm text-muted-foreground">{formatOrderMoment(order)}</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
                      <ReceiptText className="mt-0.5 h-4 w-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium">Total</p>
                        <p className="text-sm text-muted-foreground">{formatCurrency(order.totalAmount)}</p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
                      <Clock3 className="mt-0.5 h-4 w-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium">Payment</p>
                        <p className="text-sm text-muted-foreground">
                          {order.paymentMethod || "Pending selection"}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
                      <MapPinned className="mt-0.5 h-4 w-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium">Variant</p>
                        <p className="text-sm text-muted-foreground">
                          {order.variantName || "Base selection"}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : null}

          {!historyQuery.isError && !historyQuery.data?.length ? (
            <div className="rounded-xl border border-dashed p-4 text-sm text-muted-foreground">
              You have not saved any checkout drafts yet. Once you save one from checkout, it will appear here.
            </div>
          ) : null}
        </CardContent>
      </Card>
      <BookingSummaryCard />
    </div>
  );
}
