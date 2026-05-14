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
    const responseData = error?.response?.data;
    const responseStatus = responseData?.status || error?.response?.status || null;

    if (responseStatus === 401) {
      useAuthStore.getState().clearSession();
    }

    const validationErrors = responseData?.validationErrors || null;
    const firstValidationMessage = validationErrors
      ? Object.values(validationErrors)[0]
      : null;
    const message =
      firstValidationMessage ||
      responseData?.message ||
      error.message ||
      "Unexpected API error";

    const enhancedError = new Error(message);
    enhancedError.status = responseStatus;
    enhancedError.validationErrors = validationErrors;
    enhancedError.raw = responseData || null;

    return Promise.reject(enhancedError);
  }
);
