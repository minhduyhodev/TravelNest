import { useQuery } from "@tanstack/react-query";
import {
  ArrowLeft,
  CalendarDays,
  CircleCheck,
  CircleDashed,
  Clock3,
  MapPinned,
  ReceiptText,
  UserRound
} from "lucide-react";
import { NavLink, useParams } from "react-router-dom";

import { fetchBookingDetail } from "@/api/bookings";
import { queryKeys } from "@/api/queryKeys";
import { PageSkeleton } from "@/components/feedback/PageSkeleton";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getServiceTypeLabel } from "@/features/booking/draft";
import { formatCurrency } from "@/utils/currency";

function formatSchedule(booking) {
  const values = [
    booking.startDate,
    booking.endDate && booking.endDate !== booking.startDate ? `to ${booking.endDate}` : null
  ]
    .filter(Boolean)
    .join(" ");

  if (booking.serviceTime) {
    return values ? `${values} at ${booking.serviceTime}` : booking.serviceTime;
  }

  return values || "Schedule will be confirmed";
}

function buildTimeline(booking) {
  return [
    {
      key: "created",
      label: "Draft saved",
      timestamp: booking.createdAt,
      helper: "Checkout draft was converted into a booking request."
    },
    {
      key: "confirmed",
      label: "Confirmed",
      timestamp: booking.confirmedAt,
      helper: booking.assignedStaffName
        ? `Handled by ${booking.assignedStaffName}.`
        : "Staff confirmation is still pending."
    },
    {
      key: "completed",
      label: "Completed",
      timestamp: booking.completedAt,
      helper: "The service has been delivered successfully."
    },
    {
      key: "cancelled",
      label: "Cancelled",
      timestamp: booking.cancelledAt,
      helper: booking.cancelReason || "This booking was cancelled before completion."
    }
  ].filter((item) => item.key !== "cancelled" || booking.cancelledAt);
}

export function UserBookingDetailPage() {
  const { bookingId } = useParams();
  const bookingQuery = useQuery({
    queryKey: queryKeys.booking.detail(bookingId),
    queryFn: () => fetchBookingDetail(bookingId),
    enabled: Boolean(bookingId)
  });

  if (bookingQuery.isLoading) {
    return <PageSkeleton />;
  }

  if (bookingQuery.isError) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Booking detail unavailable</CardTitle>
          <CardDescription>
            We could not load the selected booking right now.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
            {bookingQuery.error.message}
          </div>
          <Button asChild variant="outline">
            <NavLink to="/account/bookings">Back to bookings</NavLink>
          </Button>
        </CardContent>
      </Card>
    );
  }

  const booking = bookingQuery.data;
  const timeline = buildTimeline(booking);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Button asChild variant="outline">
          <NavLink to="/account/bookings">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to bookings
          </NavLink>
        </Button>
        <div className="rounded-full bg-accent px-3 py-1 text-xs font-medium text-primary">
          {booking.status}
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <Card>
          <CardHeader>
            <CardTitle>{booking.serviceName}</CardTitle>
            <CardDescription>
              {getServiceTypeLabel(booking.serviceType)} booking - {booking.bookingCode}
            </CardDescription>
          </CardHeader>
          <CardContent className="grid gap-3 md:grid-cols-2">
            <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
              <CalendarDays className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-medium">Schedule</p>
                <p className="text-sm text-muted-foreground">{formatSchedule(booking)}</p>
              </div>
            </div>
            <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
              <ReceiptText className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-medium">Total</p>
                <p className="text-sm text-muted-foreground">{formatCurrency(booking.totalAmount)}</p>
              </div>
            </div>
            <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
              <Clock3 className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-medium">Payment</p>
                <p className="text-sm text-muted-foreground">{booking.paymentMethod || "Pending selection"}</p>
              </div>
            </div>
            <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
              <MapPinned className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-medium">Variant</p>
                <p className="text-sm text-muted-foreground">{booking.variantName || "Base selection"}</p>
              </div>
            </div>
            <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
              <UserRound className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-medium">Contact</p>
                <p className="text-sm text-muted-foreground">{booking.contactFullName}</p>
                <p className="text-sm text-muted-foreground">{booking.contactPhone}</p>
                <p className="text-sm text-muted-foreground">{booking.contactEmail}</p>
              </div>
            </div>
            <div className="rounded-lg border bg-surface-1 p-3">
              <p className="text-sm font-medium">Special requests</p>
              <p className="mt-1 text-sm text-muted-foreground">
                {booking.specialRequests || "No special request was added to this booking."}
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Booking timeline</CardTitle>
            <CardDescription>
              Follow the main milestones for this request from creation to fulfillment.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {timeline.map((item) => {
              const isDone = Boolean(item.timestamp);
              return (
                <div key={item.key} className="flex gap-3 rounded-xl border p-4">
                  <div className="pt-0.5 text-primary">
                    {isDone ? <CircleCheck className="h-5 w-5" /> : <CircleDashed className="h-5 w-5" />}
                  </div>
                  <div className="space-y-1">
                    <p className="font-medium">{item.label}</p>
                    <p className="text-sm text-muted-foreground">
                      {item.timestamp || "Not reached yet"}
                    </p>
                    <p className="text-sm text-muted-foreground">{item.helper}</p>
                  </div>
                </div>
              );
            })}

            {booking.staffNote || booking.assignedStaffName || booking.cancelReason ? (
              <div className="rounded-xl border bg-surface-1 p-4">
                <p className="font-medium">Operations note</p>
                {booking.assignedStaffName ? (
                  <p className="mt-2 text-sm text-muted-foreground">
                    Assigned staff: {booking.assignedStaffName}
                  </p>
                ) : null}
                {booking.staffNote ? (
                  <p className="text-sm text-muted-foreground">Staff note: {booking.staffNote}</p>
                ) : null}
                {booking.cancelReason ? (
                  <p className="text-sm text-muted-foreground">Cancel reason: {booking.cancelReason}</p>
                ) : null}
              </div>
            ) : null}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
