package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
public class BookRoomDto {

    @NotNull(message = "Provide check in time")
     @FutureOrPresent(message = "Check in Time must be of present or future")
    private LocalDate checkInDate;

    @NotNull(message = "Provide checkout time")
    @FutureOrPresent(message = "Checkout time must be of present or future")
    private LocalDate checkoutDate;

    @Pattern(regexp = "^(0?[1-9]|1[0-2]):[0-5][0-9](am|pm)$",message = "Invalid pattern in checkIn Time. Pattern eg: 12:00pm. Time limit=1-12")
  private String checkInTime;

    @Pattern(regexp = "^(0?[1-9]|1[0-2]):[0-5][0-9](am|pm)$",message = "Invalid pattern in checkout Time. Pattern eg: 12:00pm. Time limit=1-12")
  private String checkoutTime;



}
