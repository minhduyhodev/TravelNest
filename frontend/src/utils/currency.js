export function formatCurrency(value, locale = "vi") {
  const currency = locale === "vi" ? "VND" : "USD";

  return new Intl.NumberFormat(locale === "vi" ? "vi-VN" : "en-US", {
    style: "currency",
    currency,
    maximumFractionDigits: 0
  }).format(value);
}
