import { startTransition, useDeferredValue, useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";
import { useSearchParams } from "react-router-dom";

import { fetchTours } from "@/api/tours";
import { queryKeys } from "@/api/queryKeys";
import { TourCard } from "@/components/data-display/TourCard";
import { PageShell } from "@/components/layout/PageShell";
import { Input } from "@/components/ui/input";

export function TourListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const keywordParam = searchParams.get("keyword") || "";
  const [searchTerm, setSearchTerm] = useState(keywordParam);

  useEffect(() => {
    setSearchTerm(keywordParam);
  }, [keywordParam]);

  const deferredSearchTerm = useDeferredValue(searchTerm.trim());
  const tourQuery = useQuery({
    queryKey: queryKeys.tours.list({ keyword: deferredSearchTerm || null }),
    queryFn: () => fetchTours(deferredSearchTerm ? { keyword: deferredSearchTerm } : {})
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
        <h1 className="text-3xl font-semibold">Tours</h1>
        <p className="text-muted-foreground">
          Public tour catalog now reads from the Spring Boot API instead of local sample data.
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
                placeholder="Destination or tour name"
                value={searchTerm}
                onChange={handleSearchChange}
              />
            </div>
            <div className="mt-3 space-y-2 text-sm text-muted-foreground">
              <p>Duration</p>
              <p>Departure window</p>
              <p>Price range</p>
            </div>
          </div>
        </aside>
        <section className="space-y-4">
          {tourQuery.isLoading && (
            <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
              Loading tours from the TravelNest API...
            </div>
          )}
          {tourQuery.isError && (
            <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {tourQuery.error.message}
            </div>
          )}
          {!tourQuery.isLoading && !tourQuery.isError && (
            <>
              <p className="text-sm text-muted-foreground">
                {tourQuery.data?.length || 0} tours available for the current filter.
              </p>
              {tourQuery.data?.length ? (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  {tourQuery.data.map((tour) => (
                    <TourCard key={tour.slug} tour={tour} />
                  ))}
                </div>
              ) : (
                <div className="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
                  No tour matches your current search yet.
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </PageShell>
  );
}
