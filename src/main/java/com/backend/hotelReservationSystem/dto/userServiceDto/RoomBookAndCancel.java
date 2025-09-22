package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoomBookAndCancel {
    @NotNull(message = "Business does not exist")
    Long  roomNumber;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "checkIn date must be today or later")
    LocalDateTime checkInTime;

    @NotNull(message = "Check-out date is required")
    @FutureOrPresent(message = "checkout date must be today or later")
    LocalDateTime checkoutTime;

}
