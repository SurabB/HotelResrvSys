package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BookRoomDto {
    @NotNull(message = "Business does not exist")
    Long  roomNumber;

    @Positive(message = "Booking Time must be greater than 0")
    @NotNull(message = "Provide booking time")
     @Max(value = 1000L)
    Integer bookingTime;


}
