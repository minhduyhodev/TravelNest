import { useQuery } from "@tanstack/react-query";
import { Clock3, MapPin, Star, UtensilsCrossed } from "lucide-react";
import { NavLink, useNavigate, useParams } from "react-router-dom";

import { fetchRestaurantDetail } from "@/api/restaurants";
import { queryKeys } from "@/api/queryKeys";
import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { createRestaurantBookingDraft } from "@/features/booking/draft";
import { PageShell } from "@/components/layout/PageShell";
import { useBookingStore } from "@/stores/useBookingStore";
import { formatCurrency } from "@/utils/currency";

export function RestaurantDetailPage() {
  const navigate = useNavigate();
  const replaceDraft = useBookingStore((state) => state.replaceDraft);
  const { slug } = useParams();
  const restaurantQuery = useQuery({
    queryKey: queryKeys.restaurants.detail(slug),
    queryFn: () => fetchRestaurantDetail(slug),
    enabled: Boolean(slug)
  });

  if (restaurantQuery.isLoading) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Loading restaurant</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            Pulling the latest restaurant detail from the TravelNest API.
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  if (restaurantQuery.isError || !restaurantQuery.data) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Restaurant not found</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-muted-foreground">
              {restaurantQuery.error?.message || "The selected restaurant is not available right now."}
            </p>
            <Button asChild>
              <NavLink to="/restaurants">Back to restaurants</NavLink>
            </Button>
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  const restaurant = restaurantQuery.data;
  const bookingPreview = createRestaurantBookingDraft(restaurant);

  const handleContinueToBooking = () => {
    replaceDraft(bookingPreview);
    navigate("/checkout");
  };

  return (
    <PageShell className="space-y-6">
      <div className="grid gap-6 lg:grid-cols-[1.45fr_0.75fr]">
        <section className="space-y-5">
          <div className="aspect-[16/9] rounded-2xl bg-gradient-to-br from-amber-100 via-orange-50 to-white" />
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-3">
              <h1 className="text-3xl font-semibold">{restaurant.name}</h1>
              <div className="inline-flex items-center gap-1 rounded-full bg-accent px-3 py-1 text-sm font-medium text-primary">
                <Star className="h-4 w-4 fill-current" />
                {restaurant.rating}
              </div>
            </div>
            <p className="flex items-center gap-2 text-muted-foreground">
              <MapPin className="h-4 w-4" />
              {restaurant.location}
            </p>
            <p className="max-w-3xl text-muted-foreground">{restaurant.description}</p>
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Cuisine</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">{restaurant.cuisine}</CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Opening hours</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">{restaurant.schedule}</CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Menu categories</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {restaurant.menuCategories.length} dining sections
              </CardContent>
            </Card>
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Reservation notes</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 md:grid-cols-2">
              {restaurant.policies.map((policy) => (
                <div key={policy} className="rounded-xl border bg-surface-1 px-4 py-3 text-sm text-muted-foreground">
                  {policy}
                </div>
              ))}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Popular dishes</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {restaurant.menuPreview.map((dish) => (
                <div key={dish} className="rounded-xl border px-4 py-3 text-sm text-muted-foreground">
                  {dish}
                </div>
              ))}
            </CardContent>
          </Card>
        </section>
        <aside className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Reserve a table</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex items-center gap-2 rounded-md border p-3">
                <Clock3 className="h-4 w-4 text-primary" />
                {restaurant.schedule}
              </div>
              <div className="flex items-center gap-2 rounded-md border p-3">
                <UtensilsCrossed className="h-4 w-4 text-primary" />
                {restaurant.cuisine}
              </div>
              <div className="rounded-xl bg-surface-1 p-4">
                <p className="text-xs uppercase tracking-[0.2em] text-muted-foreground">Average spend</p>
                <p className="mt-1 text-2xl font-semibold text-primary">{formatCurrency(restaurant.priceFrom)}</p>
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
