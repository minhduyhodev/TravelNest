const DEFAULT_PAYMENT_METHOD = "VNPAY";

export const PAYMENT_METHOD_OPTIONS = [
  {
    value: "VNPAY",
    label: "VNPay",
    description: "Domestic cards, QR checkout, and card payments."
  },
  {
    value: "MOMO",
    label: "MoMo",
    description: "Wallet checkout for faster confirmations on mobile."
  }
];

function addDays(baseDate, days) {
  const nextDate = new Date(baseDate);
  nextDate.setDate(nextDate.getDate() + days);
  return nextDate;
}

function toIsoDate(value) {
  return value.toISOString().slice(0, 10);
}

function getNextDate(days) {
  return toIsoDate(addDays(new Date(), days));
}

function getSafeNumber(value, fallback = 0) {
  const parsedValue = Number(value);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function pluralize(value, singular, plural) {
  return `${value} ${value === 1 ? singular : plural}`;
}

function getHotelNightCount(checkInDate, checkOutDate) {
  if (!checkInDate || !checkOutDate) {
    return 1;
  }

  const startDate = new Date(checkInDate);
  const endDate = new Date(checkOutDate);
  const diffTime = endDate.getTime() - startDate.getTime();

  if (!Number.isFinite(diffTime) || diffTime <= 0) {
    return 1;
  }

  return Math.max(1, Math.round(diffTime / 86400000));
}

function createBaseDraft(serviceType, serviceId, serviceName, location, priceAmount) {
  return {
    serviceType,
    serviceId,
    serviceName,
    location,
    priceAmount,
    guestCount: 2,
    contactFullName: "",
    contactEmail: "",
    contactPhone: "",
    voucherCode: "",
    paymentMethod: DEFAULT_PAYMENT_METHOD,
    specialRequests: ""
  };
}

export function createHotelBookingDraft(hotel) {
  return {
    ...createBaseDraft("HOTEL", hotel.id, hotel.name, hotel.location, hotel.priceFrom),
    roomLabel: hotel.roomOptions?.[0] || "Standard room",
    roomCount: 1,
    checkInDate: getNextDate(7),
    checkOutDate: getNextDate(9),
    scheduleLabel: `Check-in ${hotel.checkInTime} / Check-out ${hotel.checkOutTime}`
  };
}

export function createTourBookingDraft(tour) {
  return {
    ...createBaseDraft("TOUR", tour.id, tour.name, tour.location, tour.priceFrom),
    guestCount: 2,
    departureDate: getNextDate(14),
    scheduleLabel: `${tour.departure} / ${tour.duration}`
  };
}

export function createRestaurantBookingDraft(restaurant) {
  return {
    ...createBaseDraft("RESTAURANT", restaurant.id, restaurant.name, restaurant.location, restaurant.priceFrom),
    reservationDate: getNextDate(3),
    reservationTime: "19:00",
    scheduleLabel: restaurant.schedule
  };
}

export function getServiceTypeLabel(serviceType) {
  switch (serviceType) {
    case "HOTEL":
      return "Hotel";
    case "TOUR":
      return "Tour";
    case "RESTAURANT":
      return "Restaurant";
    default:
      return "Service";
  }
}

export function getBookingTotal(draft) {
  if (!draft?.serviceType) {
    return 0;
  }

  const basePrice = getSafeNumber(draft.priceAmount);

  if (draft.serviceType === "HOTEL") {
    const nightCount = getHotelNightCount(draft.checkInDate, draft.checkOutDate);
    const roomCount = Math.max(1, getSafeNumber(draft.roomCount, 1));
    return basePrice * nightCount * roomCount;
  }

  const guestCount = Math.max(1, getSafeNumber(draft.guestCount, 1));
  return basePrice * guestCount;
}

export function getBookingSummary(draft) {
  if (!draft?.serviceType || !draft.serviceName) {
    return null;
  }

  const guestCount = Math.max(1, getSafeNumber(draft.guestCount, 1));
  const rows = [
    { label: "Service", value: draft.serviceName },
    { label: "Category", value: getServiceTypeLabel(draft.serviceType) },
    { label: "Location", value: draft.location || "Will be confirmed at checkout" }
  ];

  if (draft.serviceType === "HOTEL") {
    const roomCount = Math.max(1, getSafeNumber(draft.roomCount, 1));
    const nightCount = getHotelNightCount(draft.checkInDate, draft.checkOutDate);

    rows.push(
      {
        label: "Stay dates",
        value:
          draft.checkInDate && draft.checkOutDate
            ? `${draft.checkInDate} to ${draft.checkOutDate}`
            : "Choose your stay dates"
      },
      {
        label: "Guests and rooms",
        value: `${pluralize(guestCount, "guest", "guests")} / ${pluralize(roomCount, "room", "rooms")}`
      },
      {
        label: "Stay info",
        value: `${pluralize(nightCount, "night", "nights")} / ${draft.roomLabel || "Room selection"}`
      }
    );
  }

  if (draft.serviceType === "TOUR") {
    rows.push(
      {
        label: "Departure date",
        value: draft.departureDate || "Choose your departure date"
      },
      {
        label: "Travelers",
        value: pluralize(guestCount, "traveler", "travelers")
      },
      {
        label: "Schedule",
        value: draft.scheduleLabel || "Departure schedule will be confirmed"
      }
    );
  }

  if (draft.serviceType === "RESTAURANT") {
    rows.push(
      {
        label: "Reservation",
        value:
          draft.reservationDate && draft.reservationTime
            ? `${draft.reservationDate} at ${draft.reservationTime}`
            : "Choose your reservation slot"
      },
      {
        label: "Party size",
        value: pluralize(guestCount, "guest", "guests")
      },
      {
        label: "Dining hours",
        value: draft.scheduleLabel || "Dining schedule will be confirmed"
      }
    );
  }

  return {
    rows,
    total: getBookingTotal(draft),
    voucherCode: draft.voucherCode?.trim() || "",
    paymentMethod: draft.paymentMethod || DEFAULT_PAYMENT_METHOD
  };
}
