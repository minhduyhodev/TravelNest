package com.travelnest.booking.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingActionRequest {

    @Size(max = 2000)
    private String staffNote;

    @Size(max = 2000)
    private String cancelReason;
}
