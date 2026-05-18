export const queryKeys = {
  auth: {
    me: ["auth", "me"]
  },
  users: {
    me: ["users", "me"],
    addresses: ["users", "addresses"],
    staffRoot: ["users", "staff"],
    staff: (status) => ["users", "staff", status || "all"]
  },
  hotels: {
    list: (params) => ["hotels", "list", params],
    detail: (slug) => ["hotels", "detail", slug]
  },
  tours: {
    list: (params) => ["tours", "list", params],
    detail: (slug) => ["tours", "detail", slug]
  },
  restaurants: {
    list: (params) => ["restaurants", "list", params],
    detail: (slug) => ["restaurants", "detail", slug]
  },
  orders: {
    history: ["orders", "history"]
  },
  booking: {
    checkout: ["booking", "checkout"],
    history: ["booking", "history"],
    detail: (bookingId) => ["booking", "detail", bookingId],
    availability: (serviceType, slug, params) => ["booking", "availability", serviceType, slug, params],
    queueRoot: ["booking", "queue"],
    queue: (filters) => ["booking", "queue", filters]
  },
  dashboard: {
    user: ["dashboard", "user"],
    staff: ["dashboard", "staff"],
    admin: ["dashboard", "admin"]
  }
};
