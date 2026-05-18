import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function fetchMyBookings() {
  const { data } = await axiosClient.get(endpoints.booking.history);
  return data.data;
}

export async function fetchBookingDetail(bookingId) {
  const { data } = await axiosClient.get(endpoints.booking.detail(bookingId));
  return data.data;
}

export async function createBooking(payload) {
  const { data } = await axiosClient.post(endpoints.booking.create, payload);
  return data.data;
}

export async function fetchBookingQueue(params = {}) {
  const { data } = await axiosClient.get(endpoints.booking.queue, { params });
  return data.data;
}

export async function confirmBooking(bookingId, payload = {}) {
  const { data } = await axiosClient.patch(endpoints.booking.confirm(bookingId), payload);
  return data.data;
}

export async function cancelBooking(bookingId, payload = {}) {
  const { data } = await axiosClient.patch(endpoints.booking.cancel(bookingId), payload);
  return data.data;
}

export async function completeBooking(bookingId, payload = {}) {
  const { data } = await axiosClient.patch(endpoints.booking.complete(bookingId), payload);
  return data.data;
}
