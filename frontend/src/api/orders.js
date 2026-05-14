import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function createOrderDraft(payload) {
  const { data } = await axiosClient.post(endpoints.booking.checkout, payload);
  return data.data;
}

export async function fetchMyOrderHistory() {
  const { data } = await axiosClient.get(endpoints.booking.history);
  return data.data;
}
