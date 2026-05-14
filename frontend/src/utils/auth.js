export function getDefaultRouteByRole(role) {
  if (role === "ADMIN") {
    return "/admin";
  }

  if (role === "STAFF") {
    return "/staff";
  }

  return "/";
}
