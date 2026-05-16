import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { NavLink, useNavigate } from "react-router-dom";

import { fetchHotels } from "@/api/hotels";
import { queryKeys } from "@/api/queryKeys";
import { fetchRestaurants } from "@/api/restaurants";
import { fetchTours } from "@/api/tours";
import { PageShell } from "@/components/layout/PageShell";
import { HotelCard } from "@/components/data-display/HotelCard";
import { RestaurantCard } from "@/components/data-display/RestaurantCard";
import { TourCard } from "@/components/data-display/TourCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ROUTES } from "@/routes/paths";

export function HomePage() {
  const { t } = useTranslation("home");
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState("");
  const hotelsQuery = useQuery({
    queryKey: queryKeys.hotels.list({ featured: true }),
    queryFn: () => fetchHotels()
  });
  const toursQuery = useQuery({
    queryKey: queryKeys.tours.list({ featured: true }),
    queryFn: () => fetchTours()
  });
  const restaurantsQuery = useQuery({
    queryKey: queryKeys.restaurants.list({ featured: true }),
    queryFn: () => fetchRestaurants()
  });

  const featuredHotels = (hotelsQuery.data || []).slice(0, 3);
  const featuredTours = (toursQuery.data || []).slice(0, 3);
  const featuredRestaurants = (restaurantsQuery.data || []).slice(0, 3);

  const handleSearchSubmit = (event) => {
    event.preventDefault();

    const keyword = searchTerm.trim();
    if (!keyword) {
      return;
    }

    navigate(`${ROUTES.search}?keyword=${encodeURIComponent(keyword)}`);
  };

  return (
    <PageShell className="space-y-10">
      <Card className="overflow-hidden border-none bg-hero-glow">
        <CardContent className="grid gap-8 p-6 md:grid-cols-[1.35fr_0.95fr] md:p-10">
          <div className="space-y-4">
            <p className="text-sm uppercase tracking-[0.3em] text-primary">TravelNest</p>
            <h1 className="text-4xl font-semibold leading-tight md:text-5xl">
              {t("heroTitle")}
            </h1>
            <p className="max-w-2xl text-base text-muted-foreground md:text-lg">
              {t("heroSubtitle")}
            </p>
            <div className="flex flex-wrap gap-3">
              <Button asChild>
                <NavLink to="/hotels">Explore stays</NavLink>
              </Button>
              <Button variant="outline" asChild>
                <NavLink to="/tours">Build your itinerary</NavLink>
              </Button>
            </div>
          </div>
          <div className="rounded-xl border bg-card p-5 shadow-floating">
            <form className="space-y-3" onSubmit={handleSearchSubmit}>
              <p className="font-medium">Smart search</p>
              <Input
                placeholder="Destination, hotel, tour, restaurant"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
              />
              <div className="grid grid-cols-2 gap-3 text-sm text-muted-foreground">
                <div className="rounded-md border bg-background px-3 py-3">
                  Hotels and stays
                </div>
                <div className="rounded-md border bg-background px-3 py-3">
                  Tours and experiences
                </div>
                <div className="col-span-2 rounded-md border bg-background px-3 py-3">
                  Restaurants and reservations
                </div>
              </div>
              <Button className="w-full" type="submit" disabled={!searchTerm.trim()}>
                Search now
              </Button>
            </form>
          </div>
        </CardContent>
      </Card>

      <section className="space-y-4">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold md:text-3xl">Featured stays</h2>
            <p className="text-muted-foreground">
              Production-ready card grid for hotels, tours, and restaurants.
            </p>
          </div>
          <Button variant="ghost" asChild>
            <NavLink to="/hotels">View all</NavLink>
          </Button>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {featuredHotels.map((hotel) => (
            <HotelCard key={hotel.slug} hotel={hotel} />
          ))}
        </div>
      </section>

      <section className="space-y-4">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold md:text-3xl">Guided tours</h2>
            <p className="text-muted-foreground">
              Multi-day and day-trip catalog shells aligned with the Phase 2 roadmap.
            </p>
          </div>
          <Button variant="ghost" asChild>
            <NavLink to="/tours">View all</NavLink>
          </Button>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {featuredTours.map((tour) => (
            <TourCard key={tour.slug} tour={tour} />
          ))}
        </div>
      </section>

      <section className="space-y-4">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold md:text-3xl">Dining reservations</h2>
            <p className="text-muted-foreground">
              Restaurant discovery and reservation-ready detail pages for the third public catalog stream.
            </p>
          </div>
          <Button variant="ghost" asChild>
            <NavLink to="/restaurants">View all</NavLink>
          </Button>
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          {featuredRestaurants.map((restaurant) => (
            <RestaurantCard key={restaurant.slug} restaurant={restaurant} />
          ))}
        </div>
      </section>
    </PageShell>
  );
}
