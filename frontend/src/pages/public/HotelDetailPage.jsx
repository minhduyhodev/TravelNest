import { useQuery } from "@tanstack/react-query";
import { CalendarDays, MapPin, Star, Users } from "lucide-react";
import { NavLink, useNavigate, useParams } from "react-router-dom";

import { fetchHotelDetail } from "@/api/hotels";
import { queryKeys } from "@/api/queryKeys";
import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { PageShell } from "@/components/layout/PageShell";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { createHotelBookingDraft } from "@/features/booking/draft";
import { useBookingStore } from "@/stores/useBookingStore";
import { formatCurrency } from "@/utils/currency";

export function HotelDetailPage() {
  const navigate = useNavigate();
  const replaceDraft = useBookingStore((state) => state.replaceDraft);
  const { slug } = useParams();
  const hotelQuery = useQuery({
    queryKey: queryKeys.hotels.detail(slug),
    queryFn: () => fetchHotelDetail(slug),
    enabled: Boolean(slug)
  });

  if (hotelQuery.isLoading) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Loading hotel</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            Pulling the latest hotel detail from the TravelNest API.
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  if (hotelQuery.isError || !hotelQuery.data) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Hotel not found</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-muted-foreground">
              {hotelQuery.error?.message || "The selected hotel is not available right now."}
            </p>
            <Button asChild>
              <NavLink to="/hotels">Back to hotels</NavLink>
            </Button>
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  const hotel = hotelQuery.data;
  const bookingPreview = createHotelBookingDraft(hotel);

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
              <h1 className="text-3xl font-semibold">{hotel.name}</h1>
              <div className="inline-flex items-center gap-1 rounded-full bg-accent px-3 py-1 text-sm font-medium text-primary">
                <Star className="h-4 w-4 fill-current" />
                {hotel.rating}
              </div>
            </div>
            <p className="flex items-center gap-2 text-muted-foreground">
              <MapPin className="h-4 w-4" />
              {hotel.location}
            </p>
            <p className="max-w-3xl text-muted-foreground">{hotel.description}</p>
            <p className="text-sm font-medium text-primary">
              From {formatCurrency(hotel.priceFrom)} per stay option.
            </p>
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Room options</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {hotel.roomOptions.join(", ")}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Policies</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {hotel.policies.length} stay rules ready for checkout integration.
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Amenities</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {hotel.amenities.length} guest-facing amenities ready for filtering.
              </CardContent>
            </Card>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Top amenities</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 md:grid-cols-2">
              {hotel.amenities.map((amenity) => (
                <div key={amenity} className="rounded-xl border bg-surface-1 px-4 py-3 text-sm text-muted-foreground">
                  {amenity}
                </div>
              ))}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Stay policies</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {hotel.policies.map((policy) => (
                <div key={policy} className="rounded-xl border px-4 py-3 text-sm text-muted-foreground">
                  {policy}
                </div>
              ))}
            </CardContent>
          </Card>
        </section>
        <aside className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Reserve your stay</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex items-center justify-between rounded-md border p-3">
                <span>Starting from</span>
                <span className="font-semibold text-primary">{formatCurrency(hotel.priceFrom)}</span>
              </div>
              <div className="flex items-center gap-2 rounded-md border p-3">
                <CalendarDays className="h-4 w-4 text-primary" />
                Check-in {hotel.checkInTime} / Check-out {hotel.checkOutTime}
              </div>
              <div className="flex items-center gap-2 rounded-md border p-3">
                <Users className="h-4 w-4 text-primary" />
                {hotel.roomOptions.length} room options ready
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
