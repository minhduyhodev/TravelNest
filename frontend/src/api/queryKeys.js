export const queryKeys = {
  auth: {
    me: ["auth", "me"]
  },
  users: {
    me: ["users", "me"],
    addresses: ["users", "addresses"]
  },
  hotels: {
    list: (params) => ["hotels", "list", params],
    detail: (slug) => ["hotels", "detail", slug]
  },
  booking: {
    checkout: ["booking", "checkout"],
    history: ["booking", "history"]
  },
  dashboard: {
    user: ["dashboard", "user"],
    staff: ["dashboard", "staff"],
    admin: ["dashboard", "admin"]
  }
};
