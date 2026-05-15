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
    detail: (slug) => `/hotels/${slug}`
  },
  tours: {
    list: "/tours",
    detail: (slug) => `/tours/${slug}`
  },
  restaurants: {
    list: "/restaurants",
    detail: (slug) => `/restaurants/${slug}`
  },
  booking: {
    create: "/bookings",
    history: "/bookings/me",
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
