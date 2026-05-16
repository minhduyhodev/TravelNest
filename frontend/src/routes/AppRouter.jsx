import { Route, Routes } from "react-router-dom";

import { AdminDashboardLayout } from "@/layouts/AdminDashboardLayout";
import { AuthLayout } from "@/layouts/AuthLayout";
import { PublicLayout } from "@/layouts/PublicLayout";
import { StaffDashboardLayout } from "@/layouts/StaffDashboardLayout";
import { UserDashboardLayout } from "@/layouts/UserDashboardLayout";
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage";
import { AdminReportsPage } from "@/pages/admin/AdminReportsPage";
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage";
import { AccountProfilePage } from "@/pages/account/AccountProfilePage";
import { UserBookingsPage } from "@/pages/account/UserBookingsPage";
import { UserDashboardPage } from "@/pages/account/UserDashboardPage";
import { ForgotPasswordPage } from "@/pages/auth/ForgotPasswordPage";
import { LoginPage } from "@/pages/auth/LoginPage";
import { RegisterPage } from "@/pages/auth/RegisterPage";
import { ResetPasswordPage } from "@/pages/auth/ResetPasswordPage";
import { VerifyResetOtpPage } from "@/pages/auth/VerifyResetOtpPage";
import { BookingCheckoutPage } from "@/pages/public/BookingCheckoutPage";
import { HomePage } from "@/pages/public/HomePage";
import { HotelDetailPage } from "@/pages/public/HotelDetailPage";
import { HotelListPage } from "@/pages/public/HotelListPage";
import { RestaurantDetailPage } from "@/pages/public/RestaurantDetailPage";
import { RestaurantListPage } from "@/pages/public/RestaurantListPage";
import { SearchResultsPage } from "@/pages/public/SearchResultsPage";
import { TourDetailPage } from "@/pages/public/TourDetailPage";
import { TourListPage } from "@/pages/public/TourListPage";
import { StaffDashboardPage } from "@/pages/staff/StaffDashboardPage";
import { StaffQueuePage } from "@/pages/staff/StaffQueuePage";
import { ProtectedRoute } from "@/routes/ProtectedRoute";

export function AppRouter() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/search" element={<SearchResultsPage />} />
        <Route path="/hotels" element={<HotelListPage />} />
        <Route path="/hotels/:slug" element={<HotelDetailPage />} />
        <Route path="/tours" element={<TourListPage />} />
        <Route path="/tours/:slug" element={<TourDetailPage />} />
        <Route path="/restaurants" element={<RestaurantListPage />} />
        <Route path="/restaurants/:slug" element={<RestaurantDetailPage />} />
        <Route path="/checkout" element={<BookingCheckoutPage />} />
      </Route>

      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/verify-reset-otp" element={<VerifyResetOtpPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["CUSTOMER", "STAFF", "ADMIN"]} />}>
        <Route element={<UserDashboardLayout />}>
          <Route path="/account" element={<UserDashboardPage />} />
          <Route path="/account/profile" element={<AccountProfilePage />} />
          <Route path="/account/bookings" element={<UserBookingsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["STAFF", "ADMIN"]} />}>
        <Route element={<StaffDashboardLayout />}>
          <Route path="/staff" element={<StaffDashboardPage />} />
          <Route path="/staff/queue" element={<StaffQueuePage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
        <Route element={<AdminDashboardLayout />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/reports" element={<AdminReportsPage />} />
        </Route>
      </Route>
    </Routes>
  );
}
