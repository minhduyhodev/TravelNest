import { MetricCard } from "@/components/data-display/MetricCard";

export function StaffDashboardPage() {
  return (
    <div className="grid gap-4 md:grid-cols-3">
      <MetricCard label="Pending confirmations" value="14" helper="Needs response today" />
      <MetricCard label="Upcoming arrivals" value="08" helper="Within 24 hours" />
      <MetricCard label="Refund escalations" value="02" helper="Awaiting admin approval" />
    </div>
  );
}
