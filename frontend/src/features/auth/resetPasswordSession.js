const RESET_EMAIL_KEY = "travelnest-reset-email";
const RESET_TOKEN_KEY = "travelnest-reset-token";
const RESET_DEBUG_OTP_KEY = "travelnest-reset-debug-otp";

function normalizeEmail(email) {
  return email.trim().toLowerCase();
}

export function saveResetPasswordEmail(email) {
  if (typeof window === "undefined") {
    return;
  }

  window.sessionStorage.setItem(RESET_EMAIL_KEY, normalizeEmail(email));
}

export function saveResetDebugOtp(otp) {
  if (typeof window === "undefined") {
    return;
  }

  if (!otp) {
    window.sessionStorage.removeItem(RESET_DEBUG_OTP_KEY);
    return;
  }

  window.sessionStorage.setItem(RESET_DEBUG_OTP_KEY, otp.trim());
}

export function getResetPasswordEmail() {
  if (typeof window === "undefined") {
    return "";
  }

  return window.sessionStorage.getItem(RESET_EMAIL_KEY) || "";
}

export function getResetDebugOtp() {
  if (typeof window === "undefined") {
    return "";
  }

  return window.sessionStorage.getItem(RESET_DEBUG_OTP_KEY) || "";
}

export function saveResetPasswordSession(email, resetToken) {
  if (typeof window === "undefined") {
    return;
  }

  saveResetPasswordEmail(email);
  saveResetDebugOtp("");
  window.sessionStorage.setItem(RESET_TOKEN_KEY, resetToken);
}

export function getResetPasswordSession() {
  if (typeof window === "undefined") {
    return "";
  }

  return window.sessionStorage.getItem(RESET_TOKEN_KEY) || "";
}

export function clearResetPasswordSession() {
  if (typeof window === "undefined") {
    return;
  }

  window.sessionStorage.removeItem(RESET_EMAIL_KEY);
  window.sessionStorage.removeItem(RESET_TOKEN_KEY);
  window.sessionStorage.removeItem(RESET_DEBUG_OTP_KEY);
}
