import axios from "axios";

import { useAuthStore } from "@/stores/useAuthStore";
import { APP_CONFIG } from "@/utils/app";

export const axiosClient = axios.create({
  baseURL: APP_CONFIG.apiBaseUrl,
  timeout: 15000
});

axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  config.headers["Content-Type"] = "application/json";
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error?.response?.data?.message || error.message || "Unexpected API error";
    return Promise.reject(new Error(message));
  }
);
