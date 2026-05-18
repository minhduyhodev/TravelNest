import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function fetchTours(params = {}) {
  const { data } = await axiosClient.get(endpoints.tours.list, { params });
  return data.data;
}

export async function fetchTourDetail(slug) {
  const { data } = await axiosClient.get(endpoints.tours.detail(slug));
  return data.data;
}

export async function fetchTourAvailability(slug, params) {
  const { data } = await axiosClient.get(endpoints.tours.availability(slug), { params });
  return data.data;
}
