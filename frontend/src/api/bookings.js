import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function fetchMyBookings() {
  const { data } = await axiosClient.get(endpoints.booking.history);
  return data.data;
}

export async function createBooking(payload) {
  const { data } = await axiosClient.post(endpoints.booking.create, payload);
  return data.data;
}
