package com.backend.hotelReservationSystem.dto.businessServiceDto;

import com.backend.hotelReservationSystem.comp.AtLeastOneField;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@Getter
@Setter
@AtLeastOneField(message = "At least one field must be provided and Room Type should not be blank and its length must be less than 50 if provided")
public class RoomUpdateDto {
    @Positive(message = "Room Number should be Greater than Zero")
    @Max(value=9999999999L,message = "Room Number should not exceed ten digit")
    private Long roomNumber;

    @NotNull(message = "Invalid credentials")
    private Long activeRoomNumber;

    @Digits(integer = 6, fraction = 2,message = "Max digit before floating points is six and precision is two ")
    @Positive(message = "Price Per Hour should be Greater than Zero")
    private BigDecimal pricePerHour;

    private String roomType;
}
