package com.travelnest.booking.entity;

import com.travelnest.tour.entity.TourEntity;
import com.travelnest.tour.entity.TourSlotEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tour_bookings")
public class TourBookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private BookingEntity booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private TourEntity tour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_slot_id")
    private TourSlotEntity tourSlot;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "price_per_guest", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerGuest;
}
