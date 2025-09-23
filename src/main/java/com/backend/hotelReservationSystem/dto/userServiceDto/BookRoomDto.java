package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
@Setter
public class BookRoomDto {

    @NotNull(message = "Provide booking time")
     @FutureOrPresent
    LocalDate bookingTime;


    @Pattern(regexp = "^(0?[1-9]|1[0-2]):[0-5][0-9](am|pm)$",message = "Invalid pattern in starting Time. Pattern eg: 12:00pm. Time limit=1-12")
  String startingTime;

    @Pattern(regexp = "^(0?[1-9]|1[0-2]):[0-5][0-9](am|pm)$",message = "Invalid pattern in ending Time. Pattern eg: 12:00pm. Time limit=1-12")
  String endingTime;



}
