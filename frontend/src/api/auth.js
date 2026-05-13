import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function login(payload) {
  const { data } = await axiosClient.post(endpoints.auth.login, payload);
  return data;
}

export async function register(payload) {
  const { data } = await axiosClient.post(endpoints.auth.register, payload);
  return data;
}
