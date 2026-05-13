import { MetricCard } from "@/components/data-display/MetricCard";

export function UserDashboardPage() {
  return (
    <div className="grid gap-4 md:grid-cols-3">
      <MetricCard label="Upcoming bookings" value="03" helper="Next 30 days" />
      <MetricCard label="Completed trips" value="12" helper="Hotels, tours, restaurants" />
      <MetricCard label="Saved items" value="08" helper="Wishlist and compare list" />
    </div>
  );
}
