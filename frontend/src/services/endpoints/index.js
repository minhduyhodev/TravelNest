export const endpoints = {
  auth: {
    login: "/auth/login",
    register: "/auth/register",
    me: "/auth/me"
  },
  hotels: {
    list: "/hotels",
    detail: (slug) => `/hotels/${slug}`
  },
  booking: {
    checkout: "/orders",
    history: "/bookings"
  },
  dashboard: {
    user: "/dashboard/user",
    staff: "/dashboard/staff",
    admin: "/dashboard/admin"
  }
};
