import { useDeferredValue, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";

import { fetchHotels } from "@/api/hotels";
import { queryKeys } from "@/api/queryKeys";
import { HotelCard } from "@/components/data-display/HotelCard";
import { PageShell } from "@/components/layout/PageShell";
import { Input } from "@/components/ui/input";

export function HotelListPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const deferredSearchTerm = useDeferredValue(searchTerm.trim());
  const hotelQuery = useQuery({
    queryKey: queryKeys.hotels.list({ keyword: deferredSearchTerm || null }),
    queryFn: () => fetchHotels(deferredSearchTerm ? { keyword: deferredSearchTerm } : {})
  });

  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Hotels</h1>
        <p className="text-muted-foreground">
          Public hotel catalog now reads from the Spring Boot API instead of local sample data.
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
                placeholder="Destination or hotel name"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
              />
            </div>
            <div className="mt-3 space-y-2 text-sm text-muted-foreground">
              <p>Price range</p>
              <p>Star rating</p>
              <p>Amenities</p>
            </div>
          </div>
        </aside>
        <section className="space-y-4">
          {hotelQuery.isLoading && (
            <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
              Loading hotels from the TravelNest API...
            </div>
          )}
          {hotelQuery.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {hotelQuery.error.message}
            </div>
          )}
          {!hotelQuery.isLoading && !hotelQuery.isError && (
            <>
              <p className="text-sm text-muted-foreground">
                {hotelQuery.data?.length || 0} hotels available for the current filter.
              </p>
              {hotelQuery.data?.length ? (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  {hotelQuery.data.map((hotel) => (
                    <HotelCard key={hotel.slug} hotel={hotel} />
                  ))}
                </div>
              ) : (
                <div className="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
                  No hotel matches your current search yet.
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </PageShell>
  );
}
