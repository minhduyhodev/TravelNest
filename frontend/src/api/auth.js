import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function login(payload) {
  const { data } = await axiosClient.post(endpoints.auth.login, payload);
  return data.data;
}

export async function register(payload) {
  const { data } = await axiosClient.post(endpoints.auth.register, payload);
  return data.data;
}

export async function forgotPassword(payload) {
  const { data } = await axiosClient.post(endpoints.auth.forgotPassword, payload);
  return data.data;
}

export async function verifyResetOtp(payload) {
  const { data } = await axiosClient.post(endpoints.auth.verifyResetOtp, payload);
  return data.data;
}

export async function resetPassword(payload) {
  const { data } = await axiosClient.post(endpoints.auth.resetPassword, payload);
  return data.data;
}

export async function fetchMe() {
  const { data } = await axiosClient.get(endpoints.auth.me);
  return data.data;
}
