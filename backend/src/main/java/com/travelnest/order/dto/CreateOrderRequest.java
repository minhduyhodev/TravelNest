package com.travelnest.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank
    private String serviceType;

    @NotNull
    private Long serviceId;

    private String roomLabel;

    private Integer roomCount;

    private Integer guestCount;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private LocalDate departureDate;

    private LocalDate reservationDate;

    private LocalTime reservationTime;

    @NotBlank
    @Size(max = 150)
    private String contactFullName;

    @NotBlank
    @Size(max = 20)
    private String contactPhone;

    @NotBlank
    @Email
    @Size(max = 255)
    private String contactEmail;

    @Size(max = 50)
    private String voucherCode;

    @NotBlank
    @Size(max = 20)
    private String paymentMethod;

    @Size(max = 2000)
    private String specialRequests;
}
