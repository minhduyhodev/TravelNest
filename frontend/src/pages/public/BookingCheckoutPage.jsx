import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { NavLink } from "react-router-dom";

import { fetchHotelAvailability } from "@/api/hotels";
import { createOrderDraft } from "@/api/orders";
import { fetchRestaurantAvailability } from "@/api/restaurants";
import { fetchTourAvailability } from "@/api/tours";
import { fetchUserAddresses } from "@/api/users";
import { queryKeys } from "@/api/queryKeys";
import { BookingSummaryCard } from "@/components/data-display/BookingSummaryCard";
import { PageShell } from "@/components/layout/PageShell";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  PAYMENT_METHOD_OPTIONS,
  getServiceTypeLabel
} from "@/features/booking/draft";
import { useAuthStore } from "@/stores/useAuthStore";
import { useBookingStore } from "@/stores/useBookingStore";
import { formatCurrency } from "@/utils/currency";

function buildAvailabilityRequest(draft) {
  if (!draft?.serviceType || !draft?.serviceSlug) {
    return null;
  }

  if (
    draft.serviceType === "HOTEL"
    && draft.checkInDate
    && draft.checkOutDate
    && draft.guestCount
    && draft.roomCount
  ) {
    return {
      serviceType: "HOTEL",
      slug: draft.serviceSlug,
      params: {
        checkInDate: draft.checkInDate,
        checkOutDate: draft.checkOutDate,
        guestCount: draft.guestCount,
        roomCount: draft.roomCount,
        roomLabel: draft.roomLabel || null
      }
    };
  }

  if (draft.serviceType === "TOUR" && draft.departureDate && draft.guestCount) {
    return {
      serviceType: "TOUR",
      slug: draft.serviceSlug,
      params: {
        departureDate: draft.departureDate,
        guestCount: draft.guestCount
      }
    };
  }

  if (draft.serviceType === "RESTAURANT" && draft.reservationDate && draft.reservationTime && draft.guestCount) {
    return {
      serviceType: "RESTAURANT",
      slug: draft.serviceSlug,
      params: {
        reservationDate: draft.reservationDate,
        reservationTime: draft.reservationTime,
        guestCount: draft.guestCount
      }
    };
  }

  return null;
}

function getAvailabilitySummary(serviceType, availability) {
  if (!availability) {
    return null;
  }

  if (serviceType === "HOTEL") {
    return `${availability.availableRooms} room(s) currently open${availability.roomLabel ? ` for ${availability.roomLabel}` : ""}.`;
  }

  if (serviceType === "TOUR") {
    return `${availability.availableSeats} seat(s) currently open on this departure.`;
  }

  if (serviceType === "RESTAURANT") {
    return `${availability.availableTables} matching table(s) currently open around this time.`;
  }

  return null;
}

export function BookingCheckoutPage() {
  const queryClient = useQueryClient();
  const draft = useBookingStore((state) => state.draft);
  const setDraft = useBookingStore((state) => state.setDraft);
  const resetDraft = useBookingStore((state) => state.resetDraft);
  const currentUser = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);
  const [pageMessage, setPageMessage] = useState("");
  const [pageMessageTone, setPageMessageTone] = useState("success");
  const availabilityRequest = buildAvailabilityRequest(draft);

  const addressesQuery = useQuery({
    queryKey: queryKeys.users.addresses,
    queryFn: fetchUserAddresses,
    enabled: Boolean(accessToken)
  });
  const availabilityQuery = useQuery({
    queryKey: queryKeys.booking.availability(
      availabilityRequest?.serviceType,
      availabilityRequest?.slug,
      availabilityRequest?.params
    ),
    queryFn: () => {
      if (availabilityRequest.serviceType === "HOTEL") {
        return fetchHotelAvailability(availabilityRequest.slug, availabilityRequest.params);
      }
      if (availabilityRequest.serviceType === "TOUR") {
        return fetchTourAvailability(availabilityRequest.slug, availabilityRequest.params);
      }
      return fetchRestaurantAvailability(availabilityRequest.slug, availabilityRequest.params);
    },
    enabled: Boolean(availabilityRequest)
  });

  const createOrderMutation = useMutation({
    mutationFn: createOrderDraft,
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.orders.history });
      setPageMessageTone("success");
      setPageMessage(
        `Checkout draft ${response.orderCode} saved successfully. Estimated total ${formatCurrency(response.totalAmount)}.`
      );
      setDraft({
        orderId: response.id,
        orderCode: response.orderCode
      });
    },
    onError: (error) => {
      setPageMessageTone("error");
      setPageMessage(error.message);
    }
  });

  useEffect(() => {
    if (!currentUser) {
      return;
    }

    const nextDraft = {};

    if (!draft.contactFullName && currentUser.fullName) {
      nextDraft.contactFullName = currentUser.fullName;
    }

    if (!draft.contactEmail && currentUser.email) {
      nextDraft.contactEmail = currentUser.email;
    }

    if (!draft.contactPhone && currentUser.phone) {
      nextDraft.contactPhone = currentUser.phone;
    }

    if (Object.keys(nextDraft).length > 0) {
      setDraft(nextDraft);
    }
  }, [currentUser, draft.contactEmail, draft.contactFullName, draft.contactPhone, setDraft]);

  const defaultAddress = addressesQuery.data?.find((address) => address.isDefault);

  const updateField = (field) => (event) => {
    setDraft({ [field]: event.target.value });
  };

  const updateNumberField = (field, minimum = 1) => (event) => {
    const nextValue = Number(event.target.value);
    setDraft({ [field]: Number.isFinite(nextValue) ? Math.max(minimum, nextValue) : minimum });
  };

  const handleUseDefaultAddress = () => {
    if (!defaultAddress) {
      return;
    }

    const addressLine = [
      defaultAddress.addressLine,
      defaultAddress.ward,
      defaultAddress.district,
      defaultAddress.province
    ]
      .filter(Boolean)
      .join(", ");

    setDraft({
      contactFullName: defaultAddress.fullName,
      contactPhone: defaultAddress.phone,
      specialRequests: draft.specialRequests || `Default address: ${addressLine}`
    });
    setPageMessageTone("success");
    setPageMessage("Default address details copied into the checkout draft.");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!accessToken) {
      setPageMessageTone("error");
      setPageMessage("Please sign in before saving a checkout draft.");
      return;
    }

    if (!draft.serviceType || !draft.serviceId) {
      setPageMessageTone("error");
      setPageMessage("The selected service is missing required checkout identifiers.");
      return;
    }

    if (availabilityQuery.isFetching) {
      setPageMessageTone("error");
      setPageMessage("Please wait until the live availability check finishes.");
      return;
    }

    if (availabilityQuery.data && !availabilityQuery.data.available) {
      setPageMessageTone("error");
      setPageMessage(availabilityQuery.data.message || "The selected service is no longer available.");
      return;
    }

    try {
      await createOrderMutation.mutateAsync({
        serviceType: draft.serviceType,
        serviceId: draft.serviceId,
        roomLabel: draft.roomLabel || null,
        roomCount: draft.roomCount || null,
        guestCount: draft.guestCount || null,
        checkInDate: draft.checkInDate || null,
        checkOutDate: draft.checkOutDate || null,
        departureDate: draft.departureDate || null,
        reservationDate: draft.reservationDate || null,
        reservationTime: draft.reservationTime || null,
        contactFullName: draft.contactFullName,
        contactPhone: draft.contactPhone,
        contactEmail: draft.contactEmail,
        voucherCode: draft.voucherCode || null,
        paymentMethod: draft.paymentMethod,
        specialRequests: draft.specialRequests || null
      });
    } catch {
      return;
    }
  };

  if (!draft.serviceType) {
    return (
      <PageShell className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>No booking draft yet</CardTitle>
            <CardDescription>
              Start from a hotel, tour, or restaurant detail page to prepare a checkout selection.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-wrap gap-3">
            <Button asChild>
              <NavLink to="/hotels">Browse hotels</NavLink>
            </Button>
            <Button asChild variant="outline">
              <NavLink to="/tours">Browse tours</NavLink>
            </Button>
            <Button asChild variant="outline">
              <NavLink to="/restaurants">Browse restaurants</NavLink>
            </Button>
          </CardContent>
        </Card>
      </PageShell>
    );
  }

  return (
    <PageShell className="space-y-6">
      <div className="space-y-2">
        <h1 className="text-3xl font-semibold">Checkout</h1>
        <p className="text-muted-foreground">
          Complete your {getServiceTypeLabel(draft.serviceType).toLowerCase()} draft with guest details,
          payment preference, and contact information.
        </p>
      </div>
      {pageMessage && (
        <div
          className={`rounded-md border px-4 py-3 text-sm ${
            pageMessageTone === "error"
              ? "border-rose-200 bg-rose-50 text-rose-700"
              : "border-emerald-200 bg-emerald-50 text-emerald-700"
          }`}
        >
          {pageMessage}
        </div>
      )}
      <div className="grid gap-6 lg:grid-cols-[1.1fr_0.8fr]">
        <Card>
          <CardHeader>
            <CardTitle>Traveler details</CardTitle>
            <CardDescription>
              This checkout flow now saves a backend order draft so Phase 3 can keep moving toward full booking.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-6" onSubmit={handleSubmit}>
              <section className="space-y-4">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <h2 className="text-base font-semibold">Selection</h2>
                    <p className="text-sm text-muted-foreground">
                      Adjust quantities and timing before this draft is saved to the order pipeline.
                    </p>
                  </div>
                  <Button type="button" variant="ghost" onClick={resetDraft}>
                    Reset draft
                  </Button>
                </div>

                {draft.serviceType === "HOTEL" ? (
                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor="check-in-date">Check-in date</Label>
                      <Input
                        id="check-in-date"
                        type="date"
                        value={draft.checkInDate || ""}
                        onChange={updateField("checkInDate")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="check-out-date">Check-out date</Label>
                      <Input
                        id="check-out-date"
                        type="date"
                        value={draft.checkOutDate || ""}
                        onChange={updateField("checkOutDate")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="guest-count">Guests</Label>
                      <Input
                        id="guest-count"
                        type="number"
                        min="1"
                        value={draft.guestCount || 1}
                        onChange={updateNumberField("guestCount")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="room-count">Rooms</Label>
                      <Input
                        id="room-count"
                        type="number"
                        min="1"
                        value={draft.roomCount || 1}
                        onChange={updateNumberField("roomCount")}
                      />
                    </div>
                  </div>
                ) : null}

                {draft.serviceType === "TOUR" ? (
                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label htmlFor="departure-date">Departure date</Label>
                      <Input
                        id="departure-date"
                        type="date"
                        value={draft.departureDate || ""}
                        onChange={updateField("departureDate")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="tour-guests">Travelers</Label>
                      <Input
                        id="tour-guests"
                        type="number"
                        min="1"
                        value={draft.guestCount || 1}
                        onChange={updateNumberField("guestCount")}
                      />
                    </div>
                  </div>
                ) : null}

                {draft.serviceType === "RESTAURANT" ? (
                  <div className="grid gap-4 md:grid-cols-3">
                    <div className="space-y-2">
                      <Label htmlFor="reservation-date">Reservation date</Label>
                      <Input
                        id="reservation-date"
                        type="date"
                        value={draft.reservationDate || ""}
                        onChange={updateField("reservationDate")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="reservation-time">Time</Label>
                      <Input
                        id="reservation-time"
                        type="time"
                        value={draft.reservationTime || ""}
                        onChange={updateField("reservationTime")}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="party-size">Party size</Label>
                      <Input
                        id="party-size"
                        type="number"
                        min="1"
                        value={draft.guestCount || 1}
                        onChange={updateNumberField("guestCount")}
                      />
                    </div>
                  </div>
                ) : null}

                <div className="rounded-xl border bg-surface-1 p-4">
                  <p className="text-sm font-medium">Live availability</p>
                  {!availabilityRequest ? (
                    <p className="mt-1 text-sm text-muted-foreground">
                      Add your dates, guest count, or reservation time to run a live availability check.
                    </p>
                  ) : null}
                  {availabilityRequest && availabilityQuery.isLoading ? (
                    <p className="mt-1 text-sm text-muted-foreground">
                      Checking current availability for this selection...
                    </p>
                  ) : null}
                  {availabilityRequest && availabilityQuery.isError ? (
                    <p className="mt-1 text-sm text-rose-700">
                      {availabilityQuery.error.message}
                    </p>
                  ) : null}
                  {availabilityRequest && availabilityQuery.data ? (
                    <div
                      className={`mt-3 rounded-lg border px-3 py-3 text-sm ${
                        availabilityQuery.data.available
                          ? "border-emerald-200 bg-emerald-50 text-emerald-700"
                          : "border-rose-200 bg-rose-50 text-rose-700"
                      }`}
                    >
                      <p className="font-medium">{availabilityQuery.data.message}</p>
                      <p className="mt-1">
                        {getAvailabilitySummary(draft.serviceType, availabilityQuery.data)}
                      </p>
                    </div>
                  ) : null}
                </div>
              </section>

              <section className="space-y-4">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <h2 className="text-base font-semibold">Contact details</h2>
                    <p className="text-sm text-muted-foreground">
                      Saved profile information is prefilled when available.
                    </p>
                  </div>
                  {defaultAddress ? (
                    <Button type="button" variant="outline" size="sm" onClick={handleUseDefaultAddress}>
                      Use default address
                    </Button>
                  ) : null}
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="contact-full-name">Full name</Label>
                    <Input
                      id="contact-full-name"
                      value={draft.contactFullName || ""}
                      onChange={updateField("contactFullName")}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="contact-phone">Phone</Label>
                    <Input
                      id="contact-phone"
                      value={draft.contactPhone || ""}
                      onChange={updateField("contactPhone")}
                      required
                    />
                  </div>
                  <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="contact-email">Email</Label>
                    <Input
                      id="contact-email"
                      type="email"
                      value={draft.contactEmail || ""}
                      onChange={updateField("contactEmail")}
                      required
                    />
                  </div>
                </div>
                {addressesQuery.isLoading ? (
                  <p className="text-sm text-muted-foreground">Loading saved addresses...</p>
                ) : null}
              </section>

              <section className="space-y-4">
                <div>
                  <h2 className="text-base font-semibold">Voucher and notes</h2>
                  <p className="text-sm text-muted-foreground">
                    Voucher capture is in place so the payment phase can reuse this draft later.
                  </p>
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="voucher-code">Voucher code</Label>
                    <Input
                      id="voucher-code"
                      value={draft.voucherCode || ""}
                      onChange={updateField("voucherCode")}
                      placeholder="SUMMER2026"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="service-type">Selected service</Label>
                    <Input id="service-type" value={getServiceTypeLabel(draft.serviceType)} disabled readOnly />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="special-requests">Special requests</Label>
                  <textarea
                    id="special-requests"
                    className="min-h-28 w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    value={draft.specialRequests || ""}
                    onChange={updateField("specialRequests")}
                    placeholder="Arrival notes, dietary needs, celebration request..."
                  />
                </div>
              </section>

              <section className="space-y-4">
                <div>
                  <h2 className="text-base font-semibold">Payment method</h2>
                  <p className="text-sm text-muted-foreground">
                    Phase 4 will connect these choices to gateway APIs and callbacks.
                  </p>
                </div>
                <div className="grid gap-3 md:grid-cols-2">
                  {PAYMENT_METHOD_OPTIONS.map((option) => {
                    const isActive = draft.paymentMethod === option.value;
                    return (
                      <label
                        key={option.value}
                        className={`cursor-pointer rounded-xl border p-4 transition-colors ${
                          isActive ? "border-primary bg-primary/5" : "border-border bg-card"
                        }`}
                      >
                        <input
                          type="radio"
                          name="payment-method"
                          className="sr-only"
                          checked={isActive}
                          onChange={() => setDraft({ paymentMethod: option.value })}
                        />
                        <p className="font-medium">{option.label}</p>
                        <p className="mt-1 text-sm text-muted-foreground">{option.description}</p>
                      </label>
                    );
                  })}
                </div>
              </section>

              <Button
                className="w-full"
                type="submit"
                disabled={createOrderMutation.isPending || availabilityQuery.isFetching}
              >
                {createOrderMutation.isPending
                  ? "Saving checkout draft..."
                  : availabilityQuery.isFetching
                    ? "Checking availability..."
                    : "Save checkout draft"}
              </Button>
            </form>
          </CardContent>
        </Card>
        <BookingSummaryCard />
      </div>
    </PageShell>
  );
}
