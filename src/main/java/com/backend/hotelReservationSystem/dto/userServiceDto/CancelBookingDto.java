package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CancelBookingDto {
    @NotNull(message = "Room does not exist")
    Long  roomNumber;

    @NotNull(message = "Room does not exist")
    LocalDateTime checkInTime;

    @NotNull(message = "Room does not exist")
    LocalDateTime checkoutTime;
}
