import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CalendarDays, Clock3, ReceiptText } from "lucide-react";

import {
  cancelBooking,
  completeBooking,
  confirmBooking,
  fetchBookingQueue
} from "@/api/bookings";
import { queryKeys } from "@/api/queryKeys";
import { PageSkeleton } from "@/components/feedback/PageSkeleton";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { getServiceTypeLabel } from "@/features/booking/draft";
import { formatCurrency } from "@/utils/currency";

function formatBookingMoment(booking) {
  const datePart = booking.endDate && booking.endDate !== booking.startDate
    ? `${booking.startDate} to ${booking.endDate}`
    : booking.startDate || "Schedule pending";

  return booking.serviceTime ? `${datePart} at ${booking.serviceTime}` : datePart;
}

function getActionDraft(actionDrafts, bookingId) {
  return actionDrafts[bookingId] || { cancelReason: "", staffNote: "" };
}

export function StaffQueuePage() {
  const queryClient = useQueryClient();
  const [actionDrafts, setActionDrafts] = useState({});
  const [statusFilter, setStatusFilter] = useState("");
  const [serviceTypeFilter, setServiceTypeFilter] = useState("");
  const [serviceDateFilter, setServiceDateFilter] = useState("");
  const filters = useMemo(
    () => ({
      status: statusFilter || undefined,
      serviceDate: serviceDateFilter || undefined,
      serviceType: serviceTypeFilter || undefined
    }),
    [serviceDateFilter, serviceTypeFilter, statusFilter]
  );

  const queueQuery = useQuery({
    queryKey: queryKeys.booking.queue(filters),
    queryFn: () => fetchBookingQueue(filters)
  });

  const clearActionDraft = (bookingId) => {
    setActionDrafts((current) => {
      const next = { ...current };
      delete next[bookingId];
      return next;
    });
  };

  const updateActionDraft = (bookingId, field, value) => {
    setActionDrafts((current) => ({
      ...current,
      [bookingId]: {
        ...getActionDraft(current, bookingId),
        [field]: value
      }
    }));
  };

  const refreshQueue = () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.booking.queueRoot });
    queryClient.invalidateQueries({ queryKey: queryKeys.dashboard.staff });
  };

  const confirmMutation = useMutation({
    mutationFn: ({ bookingId, staffNote }) => confirmBooking(bookingId, { staffNote }),
    onSuccess: (_, variables) => {
      clearActionDraft(variables.bookingId);
      refreshQueue();
    }
  });
  const cancelMutation = useMutation({
    mutationFn: ({ bookingId, cancelReason, staffNote }) =>
      cancelBooking(bookingId, {
        cancelReason,
        staffNote
      }),
    onSuccess: (_, variables) => {
      clearActionDraft(variables.bookingId);
      refreshQueue();
    }
  });
  const completeMutation = useMutation({
    mutationFn: ({ bookingId, staffNote }) => completeBooking(bookingId, { staffNote }),
    onSuccess: (_, variables) => {
      clearActionDraft(variables.bookingId);
      refreshQueue();
    }
  });

  if (queueQuery.isLoading) {
    return <PageSkeleton />;
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Queue filters</CardTitle>
          <CardDescription>Refine the operational queue by status, service type, and service date.</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-3">
          <div className="space-y-2">
            <Label htmlFor="status-filter">Status</Label>
            <select
              id="status-filter"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="">All statuses</option>
              <option value="PENDING_CONFIRMATION">Pending confirmation</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="service-type-filter">Service type</Label>
            <select
              id="service-type-filter"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={serviceTypeFilter}
              onChange={(event) => setServiceTypeFilter(event.target.value)}
            >
              <option value="">All services</option>
              <option value="HOTEL">Hotel</option>
              <option value="TOUR">Tour</option>
              <option value="RESTAURANT">Restaurant</option>
            </select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="service-date-filter">Service date</Label>
            <Input
              id="service-date-filter"
              type="date"
              value={serviceDateFilter}
              onChange={(event) => setServiceDateFilter(event.target.value)}
            />
          </div>
        </CardContent>
      </Card>

      {queueQuery.isError ? (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          {queueQuery.error.message}
        </div>
      ) : null}

      {queueQuery.data?.length ? (
        <div className="space-y-4">
          {queueQuery.data.map((booking) => {
            const actionDraft = getActionDraft(actionDrafts, booking.id);
            const isMutating =
              confirmMutation.isPending || cancelMutation.isPending || completeMutation.isPending;

            return (
              <Card key={booking.id}>
                <CardContent className="space-y-4 p-6">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="space-y-1">
                      <p className="text-lg font-semibold">{booking.serviceName}</p>
                      <p className="text-sm text-muted-foreground">
                        {getServiceTypeLabel(booking.serviceType)} - {booking.bookingCode}
                      </p>
                    </div>
                    <div className="rounded-full bg-accent px-3 py-1 text-xs font-medium text-primary">
                      {booking.status}
                    </div>
                  </div>

                  <div className="grid gap-3 md:grid-cols-3">
                    <div className="flex items-start gap-3 rounded-lg border bg-surface-1 p-3">
                      <CalendarDays className="mt-0.5 h-4 w-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium">Schedule</p>
                        <p className="text-sm text-muted-foreground">{formatBookingMoment(booking)}</p>
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
                        <p className="text-sm font-medium">Contact</p>
                        <p className="text-sm text-muted-foreground">{booking.contactFullName}</p>
                        <p className="text-sm text-muted-foreground">{booking.contactPhone}</p>
                      </div>
                    </div>
                  </div>

                  <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label>Variant</Label>
                      <Input value={booking.variantName || "Base selection"} readOnly />
                    </div>
                    <div className="space-y-2">
                      <Label>Payment</Label>
                      <Input value={booking.paymentMethod || "Pending selection"} readOnly />
                    </div>
                  </div>

                  <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor={`staff-note-${booking.id}`}>Staff note</Label>
                      <Input
                        id={`staff-note-${booking.id}`}
                        value={actionDraft.staffNote}
                        onChange={(event) => updateActionDraft(booking.id, "staffNote", event.target.value)}
                        placeholder="Add an operational note"
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor={`cancel-reason-${booking.id}`}>Cancel reason</Label>
                      <Input
                        id={`cancel-reason-${booking.id}`}
                        value={actionDraft.cancelReason}
                        onChange={(event) => updateActionDraft(booking.id, "cancelReason", event.target.value)}
                        placeholder="Optional reason for cancellation"
                      />
                    </div>
                  </div>

                  {booking.assignedStaffName || booking.staffNote || booking.cancelReason ? (
                    <div className="rounded-lg border bg-surface-1 p-3 text-sm">
                      <p className="font-medium text-foreground">Operational context</p>
                      {booking.assignedStaffName ? (
                        <p className="mt-1 text-muted-foreground">Assigned staff: {booking.assignedStaffName}</p>
                      ) : null}
                      {booking.staffNote ? (
                        <p className="text-muted-foreground">Staff note: {booking.staffNote}</p>
                      ) : null}
                      {booking.cancelReason ? (
                        <p className="text-muted-foreground">Cancel reason: {booking.cancelReason}</p>
                      ) : null}
                    </div>
                  ) : null}

                  <div className="flex flex-wrap gap-3">
                    <Button
                      onClick={() =>
                        confirmMutation.mutate({
                          bookingId: booking.id,
                          staffNote: actionDraft.staffNote.trim() || undefined
                        })
                      }
                      disabled={isMutating || booking.status !== "PENDING_CONFIRMATION"}
                    >
                      Confirm
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() =>
                        completeMutation.mutate({
                          bookingId: booking.id,
                          staffNote: actionDraft.staffNote.trim() || undefined
                        })
                      }
                      disabled={isMutating || booking.status !== "CONFIRMED"}
                    >
                      Complete
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() =>
                        cancelMutation.mutate({
                          bookingId: booking.id,
                          cancelReason: actionDraft.cancelReason.trim() || undefined,
                          staffNote: actionDraft.staffNote.trim() || undefined
                        })
                      }
                      disabled={isMutating || !["PENDING_CONFIRMATION", "CONFIRMED"].includes(booking.status)}
                    >
                      Cancel
                    </Button>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      ) : !queueQuery.isError ? (
        <div className="rounded-xl border border-dashed p-6 text-sm text-muted-foreground">
          No bookings match the current queue filters.
        </div>
      ) : null}
    </div>
  );
}
