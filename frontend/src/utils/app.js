export const APP_CONFIG = {
  name: import.meta.env.VITE_APP_NAME || "TravelNest",
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  defaultLocale: import.meta.env.VITE_DEFAULT_LOCALE || "vi"
};
