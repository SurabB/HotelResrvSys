package com.backend.hotelReservationSystem.dto.businessServiceDto;

import com.backend.hotelReservationSystem.comp.AtLeastOneField;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
@AllArgsConstructor
@Getter
@Setter
@AtLeastOneField(message = "At least one field must be provided and Room Type should not be blank if provided and its length must be less than 50")
public class RoomUpdateDto {

    @NotNull(message = "Invalid credentials")
    private Long activeRoomNumber;

    @Digits(integer = 6, fraction = 2,message = "Max digit before floating points is six and precision is two ")
    @Positive(message = "Price Per Hour should be Greater than Zero")
    private BigDecimal pricePerHour;

    MultipartFile multipartFile;

    private String roomType;

    private Boolean roomIsActive;
}
