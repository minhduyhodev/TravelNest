export function PublicFooter() {
  return (
    <footer className="border-t bg-surface-1">
      <div className="container grid gap-6 py-10 md:grid-cols-4">
        <div className="space-y-2">
          <p className="font-heading text-lg font-semibold">TravelNest</p>
          <p className="text-sm text-muted-foreground">
            Hotels, tours, and restaurants in one consistent booking experience.
          </p>
        </div>
        <div className="space-y-2 text-sm text-muted-foreground">
          <p className="font-medium text-foreground">Explore</p>
          <p>Hotels</p>
          <p>Tours</p>
          <p>Restaurants</p>
        </div>
        <div className="space-y-2 text-sm text-muted-foreground">
          <p className="font-medium text-foreground">Company</p>
          <p>About</p>
          <p>Policies</p>
          <p>Contact</p>
        </div>
        <div className="space-y-2 text-sm text-muted-foreground">
          <p className="font-medium text-foreground">Support</p>
          <p>support@travelnest.vn</p>
          <p>1900-0000</p>
        </div>
      </div>
    </footer>
  );
}
