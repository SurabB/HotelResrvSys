package com.backend.hotelReservationSystem.dto.userServiceDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter

public class FindBusinessDto {
    @NotBlank(message = "Business Not Found")
    String businessName;
    @NotBlank(message = "Business Not Found")
    String city;
    @NotBlank(message = "Business Not Found")
    String location;

}
