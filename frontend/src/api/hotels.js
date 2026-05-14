import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function fetchHotels(params = {}) {
  const { data } = await axiosClient.get(endpoints.hotels.list, { params });
  return data.data;
}

export async function fetchHotelDetail(slug) {
  const { data } = await axiosClient.get(endpoints.hotels.detail(slug));
  return data.data;
}
