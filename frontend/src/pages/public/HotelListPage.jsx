import { Search } from "lucide-react";

import { HotelCard } from "@/components/data-display/HotelCard";
import { PageShell } from "@/components/layout/PageShell";
import { Input } from "@/components/ui/input";

const hotels = [
  {
    slug: "da-nang-ocean-suites",
    name: "Da Nang Ocean Suites",
    location: "Da Nang, Vietnam",
    description: "Beachfront suites with breakfast and sea-view rooms.",
    priceFrom: 2400000,
    rating: 9.2
  },
  {
    slug: "hue-riverside-boutique",
    name: "Hue Riverside Boutique",
    location: "Hue, Vietnam",
    description: "Elegant riverside property for culture-led city breaks.",
    priceFrom: 1650000,
    rating: 8.8
  },
  {
    slug: "nha-trang-sky-hotel",
    name: "Nha Trang Sky Hotel",
    location: "Nha Trang, Vietnam",
    description: "High-rise hotel with rooftop pool and flexible room types.",
    priceFrom: 2100000,
    rating: 9.0
  }
];

export function HotelListPage() {
  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Hotels</h1>
        <p className="text-muted-foreground">
          Client-side hotel listing page with filter-ready shell and pagination zone.
        </p>
      </div>
      <div className="grid gap-4 rounded-xl border bg-card p-4 md:grid-cols-[260px_1fr]">
        <aside className="space-y-4">
          <div className="rounded-lg bg-surface-1 p-4">
            <p className="mb-3 font-medium">Search and filters</p>
            <div className="relative">
              <Search className="absolute left-3 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input className="pl-9" placeholder="Destination or hotel name" />
            </div>
            <div className="mt-3 space-y-2 text-sm text-muted-foreground">
              <p>Price range</p>
              <p>Star rating</p>
              <p>Amenities</p>
            </div>
          </div>
        </aside>
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {hotels.map((hotel) => (
            <HotelCard key={hotel.slug} hotel={hotel} />
          ))}
        </section>
      </div>
    </PageShell>
  );
}
