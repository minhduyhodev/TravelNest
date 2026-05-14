import { endpoints } from "@/services/endpoints";
import { axiosClient } from "@/services/http/axiosClient";

export async function fetchCurrentUserProfile() {
  const { data } = await axiosClient.get(endpoints.users.me);
  return data.data;
}

export async function fetchStaffAccounts(status) {
  const { data } = await axiosClient.get(endpoints.users.staff, {
    params: status ? { status } : {}
  });
  return data.data;
}

export async function updateCurrentUserProfile(payload) {
  const { data } = await axiosClient.patch(endpoints.users.me, payload);
  return data.data;
}

export async function createStaffAccount(payload) {
  const { data } = await axiosClient.post("/users/staff", payload);
  return data.data;
}

export async function updateUserStatus(userId, payload) {
  const { data } = await axiosClient.patch(`/users/${userId}/status`, payload);
  return data.data;
}

export async function fetchUserAddresses() {
  const { data } = await axiosClient.get(endpoints.users.addresses);
  return data.data;
}

export async function createUserAddress(payload) {
  const { data } = await axiosClient.post(endpoints.users.addresses, payload);
  return data.data;
}

export async function updateUserAddress(addressId, payload) {
  const { data } = await axiosClient.put(endpoints.users.addressDetail(addressId), payload);
  return data.data;
}

export async function setDefaultUserAddress(addressId) {
  const { data } = await axiosClient.patch(endpoints.users.setDefaultAddress(addressId));
  return data.data;
}

export async function deleteUserAddress(addressId) {
  const { data } = await axiosClient.delete(endpoints.users.addressDetail(addressId));
  return data.data;
}
