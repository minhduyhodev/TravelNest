import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getBookingSummary, getServiceTypeLabel } from "@/features/booking/draft";
import { useBookingStore } from "@/stores/useBookingStore";
import { formatCurrency } from "@/utils/currency";

export function BookingSummaryCard({ draft: draftOverride }) {
  const storedDraft = useBookingStore((state) => state.draft);
  const draft = draftOverride || storedDraft;
  const summary = getBookingSummary(draft);

  if (!summary) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Booking summary</CardTitle>
          <CardDescription>Choose a hotel, tour, or restaurant to start a checkout draft.</CardDescription>
        </CardHeader>
        <CardContent className="rounded-xl border border-dashed p-4 text-sm text-muted-foreground">
          The checkout panel will update here once a service is selected.
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Booking summary</CardTitle>
        <CardDescription>
          Review your {getServiceTypeLabel(draft.serviceType).toLowerCase()} selection before checkout.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4 text-sm">
        {summary.rows.map((row) => (
          <div key={row.label} className="flex items-start justify-between gap-4">
            <span className="text-muted-foreground">{row.label}</span>
            <span className="text-right">{row.value}</span>
          </div>
        ))}
        <div className="flex items-start justify-between gap-4">
          <span className="text-muted-foreground">Payment</span>
          <span>{summary.paymentMethod}</span>
        </div>
        {summary.voucherCode ? (
          <div className="flex items-start justify-between gap-4 rounded-lg bg-accent/60 px-3 py-2">
            <span className="text-muted-foreground">Voucher</span>
            <span>{summary.voucherCode}</span>
          </div>
        ) : null}
        <div className="flex justify-between border-t pt-4 text-base font-semibold">
          <span>Estimated total</span>
          <span>{formatCurrency(summary.total)}</span>
        </div>
      </CardContent>
    </Card>
  );
}
