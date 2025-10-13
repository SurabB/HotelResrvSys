package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RoomBook {
    @NotNull(message = "Business does not exist")
    Long  roomNumber;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "checkIn date must be today or later")
    LocalDateTime checkInTime;

    @NotNull(message = "Check-out date is required")
    @FutureOrPresent(message = "checkout date must be today or later")
    LocalDateTime checkoutTime;

    @NotNull(message = "user must provide price to book room")
    @Positive(message = "provided amount for booking room must be positive")
    BigDecimal userProvidePrice;

}
