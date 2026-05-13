import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import { APP_CONFIG } from "@/utils/app";
import enCommon from "@/locales/en/common.json";
import enHome from "@/locales/en/home.json";
import viCommon from "@/locales/vi/common.json";
import viHome from "@/locales/vi/home.json";

i18n.use(initReactI18next).init({
  lng: APP_CONFIG.defaultLocale,
  fallbackLng: "en",
  ns: ["common", "home"],
  defaultNS: "common",
  interpolation: {
    escapeValue: false
  },
  resources: {
    vi: {
      common: viCommon,
      home: viHome
    },
    en: {
      common: enCommon,
      home: enHome
    }
  }
});

export default i18n;
