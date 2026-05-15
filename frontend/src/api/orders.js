import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function createOrderDraft(payload) {
  const { data } = await axiosClient.post(endpoints.orders.checkout, payload);
  return data.data;
}

export async function fetchMyOrderHistory() {
  const { data } = await axiosClient.get(endpoints.orders.history);
  return data.data;
}
