export const endpoints = {
  auth: {
    login: "/auth/login",
    register: "/auth/register",
    me: "/auth/me"
  },
  users: {
    me: "/users/me",
    addresses: "/users/me/addresses",
    addressDetail: (addressId) => `/users/me/addresses/${addressId}`,
    setDefaultAddress: (addressId) => `/users/me/addresses/${addressId}/default`
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
