import { useQuery } from "@tanstack/react-query";
import { CalendarDays, Clock3, MapPin, Star } from "lucide-react";
import { NavLink, useNavigate, useParams } from "react-router-dom";

import { fetchTourDetail } from "@/api/tours";
import { queryKeys } from "@/api/queryKeys";
import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { createTourBookingDraft } from "@/features/booking/draft";
import { PageShell } from "@/components/layout/PageShell";
import { useBookingStore } from "@/stores/useBookingStore";
import { formatCurrency } from "@/utils/currency";

export function TourDetailPage() {
  const navigate = useNavigate();
  const replaceDraft = useBookingStore((state) => state.replaceDraft);
  const { slug } = useParams();
  const tourQuery = useQuery({
    queryKey: queryKeys.tours.detail(slug),
    queryFn: () => fetchTourDetail(slug),
    enabled: Boolean(slug)
  });

  if (tourQuery.isLoading) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Loading tour</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            Pulling the latest tour detail from the TravelNest API.
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  if (tourQuery.isError || !tourQuery.data) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Tour not found</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-muted-foreground">
              {tourQuery.error?.message || "The selected tour is not available right now."}
            </p>
            <Button asChild>
              <NavLink to="/tours">Back to tours</NavLink>
            </Button>
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  const tour = tourQuery.data;
  const bookingPreview = createTourBookingDraft(tour);

  const handleContinueToBooking = () => {
    replaceDraft(bookingPreview);
    navigate("/checkout");
  };

  return (
    <PageShell className="space-y-6">
      <div className="grid gap-6 lg:grid-cols-[1.45fr_0.75fr]">
        <section className="space-y-5">
          <div className="aspect-[16/9] rounded-2xl bg-gradient-to-br from-sky-100 via-cyan-50 to-white" />
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-3xl font-semibold">{tour.name}</h1>
              <div className="inline-flex items-center gap-1 rounded-full bg-accent px-3 py-1 text-sm font-medium text-primary">
                <Star className="h-4 w-4 fill-current" />
                {tour.rating}
              </div>
            </div>
            <p className="flex items-center gap-2 text-muted-foreground">
              <MapPin className="h-4 w-4" />
              {tour.location}
            </p>
            <p className="max-w-3xl text-muted-foreground">{tour.description}</p>
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Duration</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">{tour.duration}</CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Departure</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {tour.departurePoint || tour.departure}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Highlights</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">{tour.highlights.length} curated inclusions</CardContent>
            </Card>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Route highlights</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 md:grid-cols-2">
              {tour.highlights.map((highlight) => (
                <div key={highlight} className="rounded-xl border bg-surface-1 px-4 py-3 text-sm text-muted-foreground">
                  {highlight}
                </div>
              ))}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Itinerary preview</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {tour.itinerary.map((day) => (
                <div key={day} className="rounded-xl border px-4 py-3 text-sm text-muted-foreground">
                  {day}
                </div>
              ))}
            </CardContent>
          </Card>
          {tour.requirements?.length ? (
            <Card>
              <CardHeader>
                <CardTitle>Requirements</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {tour.requirements.map((requirement) => (
                  <div key={requirement} className="rounded-xl border px-4 py-3 text-sm text-muted-foreground">
                    {requirement}
                  </div>
                ))}
              </CardContent>
            </Card>
          ) : null}
        </section>
        <aside className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Reserve this tour</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex items-center gap-2 rounded-md border p-3">
                <CalendarDays className="h-4 w-4 text-primary" />
                {tour.departure}
              </div>
              <div className="flex items-center gap-2 rounded-md border p-3">
                <Clock3 className="h-4 w-4 text-primary" />
                {tour.duration}
              </div>
              <div className="rounded-xl bg-surface-1 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-muted-foreground">From</p>
                <p className="mt-1 text-2xl font-semibold text-primary">{formatCurrency(tour.priceFrom)}</p>
              </div>
              <Button className="w-full" onClick={handleContinueToBooking}>
                Continue to booking
              </Button>
            </CardContent>
          </Card>
          <BookingSummaryCard draft={bookingPreview} />
        </aside>
      </div>
    </PageShell>
  );
}
