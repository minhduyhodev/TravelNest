import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { formatCurrency } from "@/utils/currency";

export function BookingSummaryCard() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Booking summary</CardTitle>
        <CardDescription>Review selected dates, guests, and payment details.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4 text-sm">
        <div className="flex justify-between">
          <span className="text-muted-foreground">Service</span>
          <span>Da Nang Ocean Suites</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">Guests</span>
          <span>2 adults</span>
        </div>
        <div className="flex justify-between">
          <span className="text-muted-foreground">Dates</span>
          <span>18 Jun - 20 Jun</span>
        </div>
        <div className="flex justify-between border-t pt-4 text-base font-semibold">
          <span>Total</span>
          <span>{formatCurrency(4800000)}</span>
        </div>
      </CardContent>
    </Card>
  );
}
