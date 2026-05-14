import { useMemo, useState } from "react";
import { Search } from "lucide-react";

import { RestaurantCard } from "@/components/data-display/RestaurantCard";
import { PageShell } from "@/components/layout/PageShell";
import { Input } from "@/components/ui/input";
import { restaurantCatalog } from "@/data/catalog";

export function RestaurantListPage() {
  const [searchTerm, setSearchTerm] = useState("");

  const restaurants = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    if (!normalizedSearch) {
      return restaurantCatalog;
    }

    return restaurantCatalog.filter((restaurant) =>
      [restaurant.name, restaurant.location, restaurant.description, restaurant.cuisine].some((field) =>
        field.toLowerCase().includes(normalizedSearch)
      )
    );
  }, [searchTerm]);

  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Restaurants</h1>
        <p className="text-muted-foreground">
          Dining catalog shell for reservation-focused discovery, cuisine filters, and service windows.
        </p>
      </div>
      <div className="grid gap-4 rounded-xl border bg-card p-4 md:grid-cols-[260px_1fr]">
        <aside className="space-y-4">
          <div className="rounded-lg bg-surface-1 p-4">
            <p className="mb-3 font-medium">Search and filters</p>
            <div className="relative">
              <Search className="absolute left-3 top-3.5 h-4 w-4 text-muted-foreground" />
              <Input
                className="pl-9"
                placeholder="Cuisine or restaurant name"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
              />
            </div>
            <div className="mt-3 space-y-2 text-sm text-muted-foreground">
              <p>Meal type</p>
              <p>Available schedule</p>
              <p>Average spend</p>
            </div>
          </div>
        </aside>
        <section className="space-y-4">
          <p className="text-sm text-muted-foreground">
            {restaurants.length} restaurants available for the current filter.
          </p>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {restaurants.map((restaurant) => (
              <RestaurantCard key={restaurant.slug} restaurant={restaurant} />
            ))}
          </div>
        </section>
      </div>
    </PageShell>
  );
}
