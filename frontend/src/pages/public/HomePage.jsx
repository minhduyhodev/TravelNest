import { useTranslation } from "react-i18next";
import { NavLink } from "react-router-dom";

import { PageShell } from "@/components/layout/PageShell";
import { HotelCard } from "@/components/data-display/HotelCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const featuredHotels = [
  {
    slug: "da-nang-ocean-suites",
    name: "Da Nang Ocean Suites",
    location: "Da Nang, Vietnam",
    description: "Modern beachfront stay with family rooms, spa, and late check-in.",
    priceFrom: 2400000,
    rating: 9.2
  },
  {
    slug: "sapa-mountain-retreat",
    name: "Sapa Mountain Retreat",
    location: "Sapa, Vietnam",
    description: "Scenic mountain lodge with curated trekking add-ons and breakfast.",
    priceFrom: 1900000,
    rating: 8.9
  },
  {
    slug: "phu-quoc-lagoon-villa",
    name: "Phu Quoc Lagoon Villa",
    location: "Phu Quoc, Vietnam",
    description: "Lagoon-facing villas for leisure travelers and couples.",
    priceFrom: 3250000,
    rating: 9.4
  }
];

export function HomePage() {
  const { t } = useTranslation("home");

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
              <Button variant="outline">Build your itinerary</Button>
            </div>
          </div>
          <div className="rounded-xl border bg-card p-5 shadow-floating">
            <div className="space-y-3">
              <p className="font-medium">Smart search</p>
              <div className="rounded-md border bg-background px-3 py-3 text-sm text-muted-foreground">
                Destination, hotel, tour, restaurant
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="rounded-md border bg-background px-3 py-3 text-sm text-muted-foreground">
                  Dates
                </div>
                <div className="rounded-md border bg-background px-3 py-3 text-sm text-muted-foreground">
                  Guests
                </div>
              </div>
              <Button className="w-full">Search now</Button>
            </div>
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
    </PageShell>
  );
}
