export const hotelCatalog = [
  {
    slug: "da-nang-ocean-suites",
    name: "Da Nang Ocean Suites",
    location: "Da Nang, Vietnam",
    description: "Modern beachfront stay with family rooms, a spa lounge, and flexible late check-in.",
    priceFrom: 2400000,
    rating: 9.2,
    starRating: 5,
    tagline: "Sunrise-facing suites five minutes from My Khe beach.",
    amenities: ["Ocean-view rooms", "Family suites", "Breakfast included", "Airport transfer"],
    policies: ["Check-in from 14:00", "Free cancellation up to 48 hours", "Early check-in on request"],
    roomOptions: ["Deluxe Ocean", "Family Suite", "Panorama King"]
  },
  {
    slug: "sapa-mountain-retreat",
    name: "Sapa Mountain Retreat",
    location: "Sapa, Vietnam",
    description: "Scenic mountain lodge with curated trekking add-ons and a warm local breakfast experience.",
    priceFrom: 1900000,
    rating: 8.9,
    starRating: 4,
    tagline: "Quiet hillside rooms overlooking terraced valleys.",
    amenities: ["Trekking concierge", "Fireplace lounge", "Mountain shuttle", "Tea tasting"],
    policies: ["Check-in from 13:00", "One-night deposit required", "Children under 6 stay free"],
    roomOptions: ["Valley Studio", "Family Loft", "Premium Ridge Room"]
  },
  {
    slug: "phu-quoc-lagoon-villa",
    name: "Phu Quoc Lagoon Villa",
    location: "Phu Quoc, Vietnam",
    description: "Lagoon-facing villas designed for leisure travelers, couples, and anniversary trips.",
    priceFrom: 3250000,
    rating: 9.4,
    starRating: 5,
    tagline: "Private villa atmosphere with sunset kayak access.",
    amenities: ["Private plunge pool", "Lagoon deck", "Couple spa", "Sunset boat service"],
    policies: ["Check-in from 15:00", "Flexible breakfast window", "Non-smoking villas only"],
    roomOptions: ["Lagoon Villa", "Garden Villa", "Signature Sunset Villa"]
  },
  {
    slug: "hue-riverside-boutique",
    name: "Hue Riverside Boutique",
    location: "Hue, Vietnam",
    description: "Elegant riverside property made for culture-led city breaks with compact premium rooms.",
    priceFrom: 1650000,
    rating: 8.8,
    starRating: 4,
    tagline: "Walkable old-town stay with boutique service touches.",
    amenities: ["Riverfront cafe", "Cycling rentals", "Concierge desk", "Quiet reading room"],
    policies: ["Check-in from 14:00", "Cancellation before 24 hours", "ID required at arrival"],
    roomOptions: ["Classic Queen", "Riverfront Deluxe", "Junior Heritage Suite"]
  }
];

export const tourCatalog = [
  {
    slug: "ha-giang-loop-escape",
    name: "Ha Giang Loop Escape",
    location: "Ha Giang, Vietnam",
    description: "Three-day highland circuit with curated viewpoints, village stays, and support crew.",
    priceFrom: 4290000,
    rating: 9.5,
    duration: "3D2N",
    departure: "Daily from Ha Giang City",
    highlights: ["Ma Pi Leng Pass", "Ethnic village homestay", "Photo support", "Breakfast and dinner"],
    itinerary: ["Day 1: City to Yen Minh", "Day 2: Dong Van to Meo Vac", "Day 3: Return through valley route"]
  },
  {
    slug: "mekong-floating-market-day",
    name: "Mekong Floating Market Day",
    location: "Can Tho, Vietnam",
    description: "A one-day river journey through floating markets, fruit gardens, and local cooking stops.",
    priceFrom: 1450000,
    rating: 8.7,
    duration: "1D",
    departure: "06:00 every morning",
    highlights: ["Cai Rang market", "Boat breakfast", "Fruit orchard visit", "Local kitchen demo"],
    itinerary: ["Sunrise market ride", "Garden and tasting stop", "Lunch and return transfer"]
  },
  {
    slug: "lan-ha-bay-slow-cruise",
    name: "Lan Ha Bay Slow Cruise",
    location: "Hai Phong, Vietnam",
    description: "Two-day bay cruise with kayaking, cave access, and a quieter route away from crowded piers.",
    priceFrom: 3890000,
    rating: 9.1,
    duration: "2D1N",
    departure: "Weekdays and weekends",
    highlights: ["Kayak session", "Sunset deck dinner", "Cave visit", "Small-group cruise"],
    itinerary: ["Day 1: Harbor to hidden coves", "Day 2: Morning paddle and brunch return"]
  }
];

export const restaurantCatalog = [
  {
    slug: "ember-riverside-grill",
    name: "Ember Riverside Grill",
    location: "Da Nang, Vietnam",
    description: "Fire-grilled seafood and sharing plates with a breezy riverfront terrace for evening bookings.",
    priceFrom: 380000,
    rating: 9.0,
    cuisine: "Seafood and grill",
    schedule: "11:00 - 22:30",
    highlights: ["Riverfront terrace", "Private dinner corner", "Fresh catch menu", "Cocktail bar"],
    menuPreview: ["Charred squid skewers", "Lemongrass clams", "Signature river prawns"]
  },
  {
    slug: "lantern-garden-bistro",
    name: "Lantern Garden Bistro",
    location: "Hoi An, Vietnam",
    description: "Garden dining with modern Vietnamese tasting plates, ideal for quiet reservations and couples.",
    priceFrom: 320000,
    rating: 8.8,
    cuisine: "Modern Vietnamese",
    schedule: "10:30 - 21:30",
    highlights: ["Lantern courtyard", "Vegetarian set menus", "Chef tasting option", "Anniversary setup"],
    menuPreview: ["Hoi An rose dumplings", "Claypot aubergine", "Coconut coffee panna cotta"]
  },
  {
    slug: "skyline-noodle-kitchen",
    name: "Skyline Noodle Kitchen",
    location: "Ho Chi Minh City, Vietnam",
    description: "High-floor noodle bar with quick reservation windows for lunch meetings and small groups.",
    priceFrom: 180000,
    rating: 8.6,
    cuisine: "Noodles and comfort food",
    schedule: "09:00 - 21:00",
    highlights: ["Fast lunch seating", "Window tables", "Small-group menu", "Late afternoon specials"],
    menuPreview: ["Lemongrass beef noodles", "Crispy spring rolls", "Black sesame ice cream"]
  }
];

export function findHotelBySlug(slug) {
  return hotelCatalog.find((hotel) => hotel.slug === slug);
}

export function findTourBySlug(slug) {
  return tourCatalog.find((tour) => tour.slug === slug);
}

export function findRestaurantBySlug(slug) {
  return restaurantCatalog.find((restaurant) => restaurant.slug === slug);
}
