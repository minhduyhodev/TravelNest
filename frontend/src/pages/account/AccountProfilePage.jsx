import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
  createUserAddress,
  deleteUserAddress,
  fetchCurrentUserProfile,
  fetchUserAddresses,
  setDefaultUserAddress,
  updateCurrentUserProfile,
  updateUserAddress
} from "@/api/users";
import { queryKeys } from "@/api/queryKeys";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuthStore } from "@/stores/useAuthStore";

const emptyProfileForm = {
  fullName: "",
  phone: "",
  avatarUrl: "",
  dateOfBirth: "",
  gender: "",
  preferredLang: "vi"
};

const emptyAddressForm = {
  label: "",
  fullName: "",
  phone: "",
  addressLine: "",
  ward: "",
  district: "",
  province: "",
  isDefault: false
};

function normalizeProfileForm(profile) {
  if (!profile) {
    return emptyProfileForm;
  }

  return {
    fullName: profile.fullName || "",
    phone: profile.phone || "",
    avatarUrl: profile.avatarUrl || "",
    dateOfBirth: profile.dateOfBirth || "",
    gender: profile.gender || "",
    preferredLang: profile.preferredLang || "vi"
  };
}

function normalizeAddressForm(address) {
  if (!address) {
    return emptyAddressForm;
  }

  return {
    label: address.label || "",
    fullName: address.fullName || "",
    phone: address.phone || "",
    addressLine: address.addressLine || "",
    ward: address.ward || "",
    district: address.district || "",
    province: address.province || "",
    isDefault: address.isDefault || false
  };
}

export function AccountProfilePage() {
  const queryClient = useQueryClient();
  const setSession = useAuthStore((state) => state.setSession);
  const accessToken = useAuthStore((state) => state.accessToken);
  const refreshToken = useAuthStore((state) => state.refreshToken);
  const currentUser = useAuthStore((state) => state.user);
  const [profileForm, setProfileForm] = useState(emptyProfileForm);
  const [profileMessage, setProfileMessage] = useState("");
  const [addressForm, setAddressForm] = useState(emptyAddressForm);
  const [addressMessage, setAddressMessage] = useState("");
  const [editingAddressId, setEditingAddressId] = useState(null);

  const profileQuery = useQuery({
    queryKey: queryKeys.users.me,
    queryFn: fetchCurrentUserProfile
  });

  const addressesQuery = useQuery({
    queryKey: queryKeys.users.addresses,
    queryFn: fetchUserAddresses
  });

  useEffect(() => {
    if (profileQuery.data) {
      setProfileForm(normalizeProfileForm(profileQuery.data));
    }
  }, [profileQuery.data]);

  const addressMutationOptions = useMemo(
    () => ({
      onSuccess: async () => {
        setAddressMessage(editingAddressId ? "Address updated successfully." : "Address created successfully.");
        setAddressForm(emptyAddressForm);
        setEditingAddressId(null);
        await queryClient.invalidateQueries({ queryKey: queryKeys.users.addresses });
      }
    }),
    [editingAddressId, queryClient]
  );

  const updateProfileMutation = useMutation({
    mutationFn: updateCurrentUserProfile,
    onSuccess: async (profile) => {
      setProfileMessage("Profile updated successfully.");
      setSession({
        accessToken,
        refreshToken,
        user: {
          ...(currentUser || {}),
          ...profile
        }
      });
      await queryClient.invalidateQueries({ queryKey: queryKeys.users.me });
    }
  });

  const createAddressMutation = useMutation({
    mutationFn: createUserAddress,
    ...addressMutationOptions
  });

  const updateAddressMutation = useMutation({
    mutationFn: ({ addressId, payload }) => updateUserAddress(addressId, payload),
    ...addressMutationOptions
  });

  const setDefaultMutation = useMutation({
    mutationFn: setDefaultUserAddress,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.users.addresses });
    }
  });

  const deleteAddressMutation = useMutation({
    mutationFn: deleteUserAddress,
    onSuccess: async () => {
      setAddressMessage("Address deleted successfully.");
      if (editingAddressId) {
        setEditingAddressId(null);
        setAddressForm(emptyAddressForm);
      }
      await queryClient.invalidateQueries({ queryKey: queryKeys.users.addresses });
    }
  });

  const handleProfileSubmit = (event) => {
    event.preventDefault();
    setProfileMessage("");
    updateProfileMutation.mutate({
      ...profileForm,
      phone: profileForm.phone || null,
      avatarUrl: profileForm.avatarUrl || null,
      dateOfBirth: profileForm.dateOfBirth || null,
      gender: profileForm.gender || null
    });
  };

  const handleAddressSubmit = (event) => {
    event.preventDefault();
    setAddressMessage("");
    const payload = {
      ...addressForm,
      label: addressForm.label || null,
      ward: addressForm.ward || null
    };

    if (editingAddressId) {
      updateAddressMutation.mutate({ addressId: editingAddressId, payload });
      return;
    }

    createAddressMutation.mutate(payload);
  };

  const startEditingAddress = (address) => {
    setAddressMessage("");
    setEditingAddressId(address.id);
    setAddressForm(normalizeAddressForm(address));
  };

  const cancelEditingAddress = () => {
    setEditingAddressId(null);
    setAddressForm(emptyAddressForm);
    setAddressMessage("");
  };

  return (
    <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
      <Card>
        <CardHeader>
          <CardTitle>Profile</CardTitle>
          <CardDescription>Update the customer profile used across booking and checkout flows.</CardDescription>
        </CardHeader>
        <CardContent>
          {profileQuery.isError && (
            <div className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {profileQuery.error.message}
            </div>
          )}
          {profileMessage && (
            <div className="mb-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
              {profileMessage}
            </div>
          )}
          <form className="grid gap-4 md:grid-cols-2" onSubmit={handleProfileSubmit}>
            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="profile-full-name">Full name</Label>
              <Input
                id="profile-full-name"
                value={profileForm.fullName}
                onChange={(event) => setProfileForm((current) => ({ ...current, fullName: event.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="profile-phone">Phone</Label>
              <Input
                id="profile-phone"
                value={profileForm.phone}
                onChange={(event) => setProfileForm((current) => ({ ...current, phone: event.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="profile-dob">Date of birth</Label>
              <Input
                id="profile-dob"
                type="date"
                value={profileForm.dateOfBirth}
                onChange={(event) =>
                  setProfileForm((current) => ({ ...current, dateOfBirth: event.target.value }))
                }
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="profile-gender">Gender</Label>
              <select
                id="profile-gender"
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                value={profileForm.gender}
                onChange={(event) => setProfileForm((current) => ({ ...current, gender: event.target.value }))}
              >
                <option value="">Select gender</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="profile-lang">Preferred language</Label>
              <select
                id="profile-lang"
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                value={profileForm.preferredLang}
                onChange={(event) =>
                  setProfileForm((current) => ({ ...current, preferredLang: event.target.value }))
                }
              >
                <option value="vi">Vietnamese</option>
                <option value="en">English</option>
              </select>
            </div>
            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="profile-avatar">Avatar URL</Label>
              <Input
                id="profile-avatar"
                value={profileForm.avatarUrl}
                onChange={(event) => setProfileForm((current) => ({ ...current, avatarUrl: event.target.value }))}
              />
            </div>
            <div className="md:col-span-2">
              <Button type="submit">
                {updateProfileMutation.isPending ? "Saving profile..." : "Save profile"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>{editingAddressId ? "Edit address" : "Add address"}</CardTitle>
            <CardDescription>Manage saved addresses for faster checkout in later booking phases.</CardDescription>
          </CardHeader>
          <CardContent>
            {(createAddressMutation.isError || updateAddressMutation.isError || deleteAddressMutation.isError) && (
              <div className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {createAddressMutation.error?.message ||
                  updateAddressMutation.error?.message ||
                  deleteAddressMutation.error?.message}
              </div>
            )}
            {addressMessage && (
              <div className="mb-4 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                {addressMessage}
              </div>
            )}
            <form className="grid gap-4" onSubmit={handleAddressSubmit}>
              <div className="space-y-2">
                <Label htmlFor="address-label">Label</Label>
                <Input
                  id="address-label"
                  value={addressForm.label}
                  onChange={(event) => setAddressForm((current) => ({ ...current, label: event.target.value }))}
                  placeholder="Home, Office, Family..."
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="address-full-name">Recipient name</Label>
                <Input
                  id="address-full-name"
                  value={addressForm.fullName}
                  onChange={(event) =>
                    setAddressForm((current) => ({ ...current, fullName: event.target.value }))
                  }
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="address-phone">Phone</Label>
                <Input
                  id="address-phone"
                  value={addressForm.phone}
                  onChange={(event) => setAddressForm((current) => ({ ...current, phone: event.target.value }))}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="address-line">Address line</Label>
                <Input
                  id="address-line"
                  value={addressForm.addressLine}
                  onChange={(event) =>
                    setAddressForm((current) => ({ ...current, addressLine: event.target.value }))
                  }
                  required
                />
              </div>
              <div className="grid gap-4 md:grid-cols-3">
                <div className="space-y-2">
                  <Label htmlFor="address-ward">Ward</Label>
                  <Input
                    id="address-ward"
                    value={addressForm.ward}
                    onChange={(event) =>
                      setAddressForm((current) => ({ ...current, ward: event.target.value }))
                    }
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="address-district">District</Label>
                  <Input
                    id="address-district"
                    value={addressForm.district}
                    onChange={(event) =>
                      setAddressForm((current) => ({ ...current, district: event.target.value }))
                    }
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="address-province">Province</Label>
                  <Input
                    id="address-province"
                    value={addressForm.province}
                    onChange={(event) =>
                      setAddressForm((current) => ({ ...current, province: event.target.value }))
                    }
                    required
                  />
                </div>
              </div>
              <label className="flex items-center gap-2 text-sm text-muted-foreground">
                <input
                  type="checkbox"
                  checked={addressForm.isDefault}
                  onChange={(event) =>
                    setAddressForm((current) => ({ ...current, isDefault: event.target.checked }))
                  }
                />
                Set as default address
              </label>
              <div className="flex flex-wrap gap-3">
                <Button type="submit">
                  {createAddressMutation.isPending || updateAddressMutation.isPending
                    ? "Saving address..."
                    : editingAddressId
                      ? "Update address"
                      : "Add address"}
                </Button>
                {editingAddressId && (
                  <Button type="button" variant="outline" onClick={cancelEditingAddress}>
                    Cancel edit
                  </Button>
                )}
              </div>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Saved addresses</CardTitle>
            <CardDescription>Default addresses stay pinned to the top for future checkout steps.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {addressesQuery.isLoading && (
              <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
                Loading address book...
              </div>
            )}
            {addressesQuery.isError && (
              <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {addressesQuery.error.message}
              </div>
            )}
            {!addressesQuery.isLoading && addressesQuery.data?.length === 0 && (
              <div className="rounded-md border border-dashed p-4 text-sm text-muted-foreground">
                No saved address yet. Add your first address to complete the account setup.
              </div>
            )}
            {addressesQuery.data?.map((address) => (
              <div key={address.id} className="rounded-xl border p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <p className="font-medium">{address.fullName}</p>
                      {address.isDefault && (
                        <span className="rounded-full bg-accent px-2 py-1 text-xs font-medium text-foreground">
                          Default
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">{address.phone}</p>
                    <p className="text-sm text-muted-foreground">
                      {[address.addressLine, address.ward, address.district, address.province]
                        .filter(Boolean)
                        .join(", ")}
                    </p>
                    {address.label && <p className="text-xs uppercase tracking-wide text-muted-foreground">{address.label}</p>}
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {!address.isDefault && (
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => setDefaultMutation.mutate(address.id)}
                      >
                        {setDefaultMutation.isPending ? "Updating..." : "Make default"}
                      </Button>
                    )}
                    <Button type="button" variant="outline" size="sm" onClick={() => startEditingAddress(address)}>
                      Edit
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => deleteAddressMutation.mutate(address.id)}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
