import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { PageShell } from "@/components/layout/PageShell";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function BookingCheckoutPage() {
  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Checkout</h1>
        <p className="text-muted-foreground">
          Booking contact form, payment selection, voucher input, and final confirmation.
        </p>
      </div>
      <div className="grid gap-6 lg:grid-cols-[1.1fr_0.8fr]">
        <Card>
          <CardHeader>
            <CardTitle>Traveler details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 text-sm text-muted-foreground">
            <div className="rounded-md border p-4">Contact form area</div>
            <div className="rounded-md border p-4">Voucher input area</div>
            <div className="rounded-md border p-4">Payment gateway selection: VNPay / MoMo</div>
            <Button className="w-full">Pay and confirm booking</Button>
          </CardContent>
        </Card>
        <BookingSummaryCard />
      </div>
    </PageShell>
  );
}
