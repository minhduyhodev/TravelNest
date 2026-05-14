import { useMemo, useState } from "react";
import { Search } from "lucide-react";

import { TourCard } from "@/components/data-display/TourCard";
import { PageShell } from "@/components/layout/PageShell";
import { Input } from "@/components/ui/input";
import { tourCatalog } from "@/data/catalog";

export function TourListPage() {
  const [searchTerm, setSearchTerm] = useState("");

  const tours = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    if (!normalizedSearch) {
      return tourCatalog;
    }

    return tourCatalog.filter((tour) =>
      [tour.name, tour.location, tour.description, tour.duration, tour.departure].some((field) =>
        field.toLowerCase().includes(normalizedSearch)
      )
    );
  }, [searchTerm]);

  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Tours</h1>
        <p className="text-muted-foreground">
          Phase 2 public catalog shell for itinerary-led products with search-ready filters.
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
                onChange={(event) => setSearchTerm(event.target.value)}
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
          <p className="text-sm text-muted-foreground">{tours.length} tours available for the current filter.</p>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {tours.map((tour) => (
              <TourCard key={tour.slug} tour={tour} />
            ))}
          </div>
        </section>
      </div>
    </PageShell>
  );
}
