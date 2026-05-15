import { useQuery } from "@tanstack/react-query";

import { fetchBookingQueue } from "@/api/bookings";
import { queryKeys } from "@/api/queryKeys";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { PageSkeleton } from "@/components/feedback/PageSkeleton";
import { MetricCard } from "@/components/data-display/MetricCard";
import { getServiceTypeLabel } from "@/features/booking/draft";

function getTodayKey() {
  return new Date().toISOString().slice(0, 10);
}

function isWithinNextWeek(dateText) {
  if (!dateText) {
    return false;
  }

  const today = new Date();
  const todayKey = getTodayKey();
  const upperBound = new Date(today);
  upperBound.setDate(today.getDate() + 7);
  const upperBoundKey = upperBound.toISOString().slice(0, 10);
  return dateText >= todayKey && dateText <= upperBoundKey;
}

export function StaffDashboardPage() {
  const queueQuery = useQuery({
    queryKey: queryKeys.dashboard.staff,
    queryFn: () => fetchBookingQueue()
  });

  if (queueQuery.isLoading) {
    return <PageSkeleton />;
  }

  if (queueQuery.isError) {
    return (
      <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
        {queueQuery.error.message}
      </div>
    );
  }

  const bookings = queueQuery.data || [];
  const todayKey = getTodayKey();
  const pendingCount = bookings.filter((booking) => booking.status === "PENDING_CONFIRMATION").length;
  const confirmedCount = bookings.filter((booking) => booking.status === "CONFIRMED").length;
  const completedCount = bookings.filter((booking) => booking.status === "COMPLETED").length;
  const todayCount = bookings.filter((booking) => booking.startDate === todayKey).length;
  const nextWeekBookings = bookings
    .filter((booking) => isWithinNextWeek(booking.startDate))
    .sort((left, right) => (left.startDate || "").localeCompare(right.startDate || ""))
    .slice(0, 5);

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          label="Pending confirmations"
          value={String(pendingCount).padStart(2, "0")}
          helper="Needs response from staff"
        />
        <MetricCard
          label="Service today"
          value={String(todayCount).padStart(2, "0")}
          helper="Bookings scheduled for today"
        />
        <MetricCard
          label="Confirmed bookings"
          value={String(confirmedCount).padStart(2, "0")}
          helper="Ready for service delivery"
        />
        <MetricCard
          label="Completed bookings"
          value={String(completedCount).padStart(2, "0")}
          helper="Finished operational flow"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Upcoming week</CardTitle>
          <CardDescription>Closest bookings that staff should keep on the radar for the next seven days.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {nextWeekBookings.length ? (
            nextWeekBookings.map((booking) => (
              <div key={booking.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border p-4">
                <div>
                  <p className="font-medium">{booking.serviceName}</p>
                  <p className="text-sm text-muted-foreground">
                    {getServiceTypeLabel(booking.serviceType)} - {booking.startDate || "Schedule pending"}
                  </p>
                </div>
                <div className="rounded-full bg-accent px-3 py-1 text-xs font-medium text-primary">
                  {booking.status}
                </div>
              </div>
            ))
          ) : (
            <div className="rounded-xl border border-dashed p-4 text-sm text-muted-foreground">
              No bookings are scheduled in the next seven days.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
