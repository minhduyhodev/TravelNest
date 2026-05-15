package com.travelnest.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private String orderCode;
    private String status;
    private String serviceType;
    private Long serviceId;
    private String serviceName;
    private Long variantId;
    private String variantName;
    private Integer quantity;
    private Integer guestCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime serviceTime;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String contactFullName;
    private String contactPhone;
    private String contactEmail;
    private String specialRequests;
    private LocalDateTime createdAt;
    private String assignedStaffName;
    private String staffNote;
    private String cancelReason;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
}
