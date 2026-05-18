export const endpoints = {
  auth: {
    login: "/auth/login",
    register: "/auth/register",
    me: "/auth/me",
    forgotPassword: "/auth/forgot-password",
    verifyResetOtp: "/auth/verify-reset-otp",
    resetPassword: "/auth/reset-password"
  },
  users: {
    me: "/users/me",
    staff: "/users/staff",
    addresses: "/users/me/addresses",
    addressDetail: (addressId) => `/users/me/addresses/${addressId}`,
    setDefaultAddress: (addressId) => `/users/me/addresses/${addressId}/default`
  },
  hotels: {
    list: "/hotels",
    detail: (slug) => `/hotels/${slug}`,
    availability: (slug) => `/hotels/${slug}/availability`
  },
  tours: {
    list: "/tours",
    detail: (slug) => `/tours/${slug}`,
    availability: (slug) => `/tours/${slug}/availability`
  },
  restaurants: {
    list: "/restaurants",
    detail: (slug) => `/restaurants/${slug}`,
    availability: (slug) => `/restaurants/${slug}/availability`
  },
  booking: {
    create: "/bookings",
    history: "/bookings/me",
    detail: (bookingId) => `/bookings/${bookingId}`,
    queue: "/bookings",
    confirm: (bookingId) => `/bookings/${bookingId}/confirm`,
    cancel: (bookingId) => `/bookings/${bookingId}/cancel`,
    complete: (bookingId) => `/bookings/${bookingId}/complete`
  },
  orders: {
    checkout: "/orders",
    history: "/orders/me"
  },
  dashboard: {
    user: "/dashboard/user",
    staff: "/dashboard/staff",
    admin: "/dashboard/admin"
  }
};
