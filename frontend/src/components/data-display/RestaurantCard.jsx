import { Clock3, MapPin, Star } from "lucide-react";
import { NavLink } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { formatCurrency } from "@/utils/currency";

export function RestaurantCard({ restaurant }) {
  return (
    <Card className="overflow-hidden">
      <div className="aspect-[4/3] bg-gradient-to-br from-amber-100 via-orange-50 to-white" />
      <CardHeader>
        <div className="flex items-start justify-between gap-4">
          <div>
            <CardTitle>{restaurant.name}</CardTitle>
            <CardDescription className="mt-1 flex items-center gap-2">
              <MapPin className="h-4 w-4" />
              {restaurant.location}
            </CardDescription>
          </div>
          <div className="rounded-full bg-accent px-3 py-1 text-sm font-medium text-primary">
            <span className="inline-flex items-center gap-1">
              <Star className="h-4 w-4 fill-current" />
              {restaurant.rating}
            </span>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-sm text-muted-foreground">{restaurant.description}</p>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Clock3 className="h-4 w-4 text-primary" />
          <span>{restaurant.schedule}</span>
          <span className="text-border">|</span>
          <span>{restaurant.cuisine}</span>
        </div>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-muted-foreground">Average spend</p>
            <p className="text-xl font-semibold text-primary">{formatCurrency(restaurant.priceFrom)}</p>
          </div>
          <Button asChild>
            <NavLink to={`/restaurants/${restaurant.slug}`}>View detail</NavLink>
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
