import { startTransition, useDeferredValue, useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";
import { useSearchParams } from "react-router-dom";

import { fetchRestaurants } from "@/api/restaurants";
import { queryKeys } from "@/api/queryKeys";
import { RestaurantCard } from "@/components/data-display/RestaurantCard";
import { PageShell } from "@/components/layout/PageShell";
import { Input } from "@/components/ui/input";

export function RestaurantListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const keywordParam = searchParams.get("keyword") || "";
  const [searchTerm, setSearchTerm] = useState(keywordParam);

  useEffect(() => {
    setSearchTerm(keywordParam);
  }, [keywordParam]);

  const deferredSearchTerm = useDeferredValue(searchTerm.trim());
  const restaurantQuery = useQuery({
    queryKey: queryKeys.restaurants.list({ keyword: deferredSearchTerm || null }),
    queryFn: () => fetchRestaurants(deferredSearchTerm ? { keyword: deferredSearchTerm } : {})
  });

  const handleSearchChange = (event) => {
    const nextValue = event.target.value;
    setSearchTerm(nextValue);

    startTransition(() => {
      const nextParams = new URLSearchParams(searchParams);
      const normalizedKeyword = nextValue.trim();

      if (normalizedKeyword) {
        nextParams.set("keyword", normalizedKeyword);
      } else {
        nextParams.delete("keyword");
      }

      setSearchParams(nextParams, { replace: true });
    });
  };

  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Restaurants</h1>
        <p className="text-muted-foreground">
          Public restaurant catalog now reads from the Spring Boot API instead of local sample data.
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
                onChange={handleSearchChange}
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
          {restaurantQuery.isLoading && (
            <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
              Loading restaurants from the TravelNest API...
            </div>
          )}
          {restaurantQuery.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {restaurantQuery.error.message}
            </div>
          )}
          {!restaurantQuery.isLoading && !restaurantQuery.isError && (
            <>
              <p className="text-sm text-muted-foreground">
                {restaurantQuery.data?.length || 0} restaurants available for the current filter.
              </p>
              {restaurantQuery.data?.length ? (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  {restaurantQuery.data.map((restaurant) => (
                    <RestaurantCard key={restaurant.slug} restaurant={restaurant} />
                  ))}
                </div>
              ) : (
                <div className="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
                  No restaurant matches your current search yet.
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </PageShell>
  );
}
