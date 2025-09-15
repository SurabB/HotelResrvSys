package com.backend.hotelReservationSystem.dto.businessServiceDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoomAcceptorDto {
    @Positive(message = "Room number should be positive")
    @NotNull(message = "room number should not be null or empty")
    @Max( value = 9999999999L, message = "roomNumber should not exceed ten characters")
    private Long roomNumber;

    @NotNull(message = "price Per Hour should not be empty")
    @Positive(message = "Price per Hour should be positive")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal pricePerHour;

    @Size(min=1,max = 50,message = "Room type should be within 1 to 50 characters")
    @NotBlank(message = "Room type should not be empty")
    private String roomType;

    private boolean isActive;


}
