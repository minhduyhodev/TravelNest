package com.travelnest.booking.entity;

import com.travelnest.order.entity.OrderEntity;
import com.travelnest.order.entity.OrderItemEntity;
import com.travelnest.user.entity.BaseAuditEntity;
import com.travelnest.user.entity.UserEntity;
import jakarta.persistence.CascadeType;
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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class BookingEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 30)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItemEntity orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "service_type", nullable = false, length = 20)
    private String serviceType;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "contact_name", nullable = false, length = 150)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private UserEntity staff;

    @Column(name = "staff_note", columnDefinition = "TEXT")
    private String staffNote;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private HotelBookingEntity hotelBooking;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TourBookingEntity tourBooking;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RestaurantBookingEntity restaurantBooking;

    public void setHotelBooking(HotelBookingEntity hotelBooking) {
        this.hotelBooking = hotelBooking;
        if (hotelBooking != null) {
            hotelBooking.setBooking(this);
        }
    }

    public void setTourBooking(TourBookingEntity tourBooking) {
        this.tourBooking = tourBooking;
        if (tourBooking != null) {
            tourBooking.setBooking(this);
        }
    }

    public void setRestaurantBooking(RestaurantBookingEntity restaurantBooking) {
        this.restaurantBooking = restaurantBooking;
        if (restaurantBooking != null) {
            restaurantBooking.setBooking(this);
        }
    }
}
