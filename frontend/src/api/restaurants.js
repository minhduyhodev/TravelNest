import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function fetchRestaurants(params = {}) {
  const { data } = await axiosClient.get(endpoints.restaurants.list, { params });
  return data.data;
}

export async function fetchRestaurantDetail(slug) {
  const { data } = await axiosClient.get(endpoints.restaurants.detail(slug));
  return data.data;
}

export async function fetchRestaurantAvailability(slug, params) {
  const { data } = await axiosClient.get(endpoints.restaurants.availability(slug), { params });
  return data.data;
}
