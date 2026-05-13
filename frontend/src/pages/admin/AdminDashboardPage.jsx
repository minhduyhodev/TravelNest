import { MetricCard } from "@/components/data-display/MetricCard";

export function AdminDashboardPage() {
  return (
    <div className="grid gap-4 md:grid-cols-4">
      <MetricCard label="Revenue today" value="245M" helper="Gross booking value" />
      <MetricCard label="New bookings" value="128" helper="Across all services" />
      <MetricCard label="New users" value="32" helper="Last 24 hours" />
      <MetricCard label="Cancellation rate" value="4.2%" helper="Weekly trend" />
    </div>
  );
}
