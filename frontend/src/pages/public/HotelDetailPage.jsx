import { CalendarDays, MapPin, Star, Users } from "lucide-react";

import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { PageShell } from "@/components/layout/PageShell";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function HotelDetailPage() {
  return (
    <PageShell className="space-y-6">
      <div className="grid gap-6 lg:grid-cols-[1.45fr_0.75fr]">
        <section className="space-y-5">
          <div className="aspect-[16/9] rounded-2xl bg-surface-2" />
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-3xl font-semibold">Da Nang Ocean Suites</h1>
              <div className="inline-flex items-center gap-1 rounded-full bg-accent px-3 py-1 text-sm font-medium text-primary">
                <Star className="h-4 w-4 fill-current" />
                9.2
              </div>
            </div>
            <p className="flex items-center gap-2 text-muted-foreground">
              <MapPin className="h-4 w-4" />
              Da Nang, Vietnam
            </p>
            <p className="max-w-3xl text-muted-foreground">
              Detailed service page shell with gallery, policies, amenities, room types,
              ratings, and booking widgets.
            </p>
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Room options</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Deluxe, Family, and Ocean View inventory.
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Policies</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Check-in, cancellation, and booking rules.
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Reviews</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                Verified guest reviews and helpful votes.
              </CardContent>
            </Card>
          </div>
        </section>
        <aside className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Reserve your stay</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex items-center gap-2 rounded-md border p-3">
                <CalendarDays className="h-4 w-4 text-primary" />
                18 Jun - 20 Jun
              </div>
              <div className="flex items-center gap-2 rounded-md border p-3">
                <Users className="h-4 w-4 text-primary" />
                2 adults, 1 room
              </div>
              <Button className="w-full">Continue to booking</Button>
            </CardContent>
          </Card>
          <BookingSummaryCard />
        </aside>
      </div>
    </PageShell>
  );
}
