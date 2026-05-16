import { useDeferredValue } from "react";
import { useQuery } from "@tanstack/react-query";
import { NavLink, useSearchParams } from "react-router-dom";

import { fetchHotels } from "@/api/hotels";
import { queryKeys } from "@/api/queryKeys";
import { fetchRestaurants } from "@/api/restaurants";
import { fetchTours } from "@/api/tours";
import { GlobalSearchForm } from "@/components/forms/GlobalSearchForm";
import { HotelCard } from "@/components/data-display/HotelCard";
import { RestaurantCard } from "@/components/data-display/RestaurantCard";
import { TourCard } from "@/components/data-display/TourCard";
import { PageShell } from "@/components/layout/PageShell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ROUTES } from "@/routes/paths";

function buildCatalogSearchPath(basePath, keyword) {
  if (!keyword) {
    return basePath;
  }

  return `${basePath}?keyword=${encodeURIComponent(keyword)}`;
}

function SearchResultSection({ title, description, items, isLoading, isError, errorMessage, action, children }) {
  return (
    <section className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-2xl font-semibold">{title}</h2>
          <p className="text-sm text-muted-foreground">{description}</p>
        </div>
        {action}
      </div>

      {isLoading && (
        <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
          Loading matching results...
        </div>
      )}

      {isError && (
        <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {errorMessage}
        </div>
      )}

      {!isLoading && !isError && (
        items.length ? children : (
          <div className="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
            No matching results in this category yet.
          </div>
        )
      )}
    </section>
  );
}

export function SearchResultsPage() {
  const [searchParams] = useSearchParams();
  const keywordParam = searchParams.get("keyword")?.trim() || "";
  const deferredKeyword = useDeferredValue(keywordParam);
  const hasKeyword = deferredKeyword.length > 0;

  const hotelQuery = useQuery({
    queryKey: queryKeys.hotels.list({ keyword: deferredKeyword || null }),
    queryFn: () => fetchHotels({ keyword: deferredKeyword }),
    enabled: hasKeyword
  });
  const tourQuery = useQuery({
    queryKey: queryKeys.tours.list({ keyword: deferredKeyword || null }),
    queryFn: () => fetchTours({ keyword: deferredKeyword }),
    enabled: hasKeyword
  });
  const restaurantQuery = useQuery({
    queryKey: queryKeys.restaurants.list({ keyword: deferredKeyword || null }),
    queryFn: () => fetchRestaurants({ keyword: deferredKeyword }),
    enabled: hasKeyword
  });

  const hotels = hotelQuery.data || [];
  const tours = tourQuery.data || [];
  const restaurants = restaurantQuery.data || [];
  const totalResults = hotels.length + tours.length + restaurants.length;
  const isSearching = hotelQuery.isLoading || tourQuery.isLoading || restaurantQuery.isLoading;

  return (
    <PageShell className="space-y-8">
      <Card className="overflow-hidden border-none bg-hero-glow">
        <CardContent className="space-y-5 p-6 md:p-8">
          <div className="space-y-2">
            <p className="text-sm uppercase tracking-[0.3em] text-primary">Unified search</p>
            <h1 className="text-3xl font-semibold md:text-4xl">Find hotels, tours, and dining in one place</h1>
            <p className="max-w-3xl text-sm text-muted-foreground md:text-base">
              Search once and compare results across the three public catalog streams already connected to the TravelNest API.
            </p>
          </div>

          <GlobalSearchForm
            initialValue={keywordParam}
            placeholder="Try Da Nang, beach, seafood, or spa"
          />

          <div className="flex flex-wrap gap-2">
            <Button variant="outline" asChild>
              <NavLink to={buildCatalogSearchPath(ROUTES.hotels, keywordParam)}>Hotels only</NavLink>
            </Button>
            <Button variant="outline" asChild>
              <NavLink to={buildCatalogSearchPath(ROUTES.tours, keywordParam)}>Tours only</NavLink>
            </Button>
            <Button variant="outline" asChild>
              <NavLink to={buildCatalogSearchPath(ROUTES.restaurants, keywordParam)}>Restaurants only</NavLink>
            </Button>
          </div>
        </CardContent>
      </Card>

      {!hasKeyword && (
        <div className="rounded-xl border border-dashed bg-card p-6 text-sm text-muted-foreground">
          Enter a destination, service name, or travel theme to start searching across all service categories.
        </div>
      )}

      {hasKeyword && (
        <>
          <div className="flex flex-col gap-2 rounded-xl border bg-card p-5 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p className="text-sm uppercase tracking-[0.2em] text-primary">Search snapshot</p>
              <h2 className="text-2xl font-semibold">
                {isSearching
                  ? `Searching for "${keywordParam}"`
                  : `${totalResults} result${totalResults === 1 ? "" : "s"} for "${keywordParam}"`}
              </h2>
            </div>
            <p className="text-sm text-muted-foreground">
              Hotels: {hotels.length} | Tours: {tours.length} | Restaurants: {restaurants.length}
            </p>
          </div>

          <SearchResultSection
            title="Hotels"
            description="Stay options that match the current destination or keyword."
            items={hotels}
            isLoading={hotelQuery.isLoading}
            isError={hotelQuery.isError}
            errorMessage={hotelQuery.error?.message || "Unable to load hotel results."}
            action={(
              <Button variant="ghost" asChild>
                <NavLink to={buildCatalogSearchPath(ROUTES.hotels, keywordParam)}>Open hotel catalog</NavLink>
              </Button>
            )}
          >
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {hotels.map((hotel) => (
                <HotelCard key={hotel.slug} hotel={hotel} />
              ))}
            </div>
          </SearchResultSection>

          <SearchResultSection
            title="Tours"
            description="Experiences and guided itineraries related to your search."
            items={tours}
            isLoading={tourQuery.isLoading}
            isError={tourQuery.isError}
            errorMessage={tourQuery.error?.message || "Unable to load tour results."}
            action={(
              <Button variant="ghost" asChild>
                <NavLink to={buildCatalogSearchPath(ROUTES.tours, keywordParam)}>Open tour catalog</NavLink>
              </Button>
            )}
          >
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {tours.map((tour) => (
                <TourCard key={tour.slug} tour={tour} />
              ))}
            </div>
          </SearchResultSection>

          <SearchResultSection
            title="Restaurants"
            description="Dining options and reservation-friendly venues that fit the search."
            items={restaurants}
            isLoading={restaurantQuery.isLoading}
            isError={restaurantQuery.isError}
            errorMessage={restaurantQuery.error?.message || "Unable to load restaurant results."}
            action={(
              <Button variant="ghost" asChild>
                <NavLink to={buildCatalogSearchPath(ROUTES.restaurants, keywordParam)}>Open restaurant catalog</NavLink>
              </Button>
            )}
          >
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {restaurants.map((restaurant) => (
                <RestaurantCard key={restaurant.slug} restaurant={restaurant} />
              ))}
            </div>
          </SearchResultSection>
        </>
      )}
    </PageShell>
  );
}
