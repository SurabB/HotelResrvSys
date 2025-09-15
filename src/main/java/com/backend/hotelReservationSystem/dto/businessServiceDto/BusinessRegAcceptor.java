package com.backend.hotelReservationSystem.dto.businessServiceDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BusinessRegAcceptor {
    @NotBlank(message = "Business Name should not be blank or empty")
    @Size(max = 50,message = "businessName should not exceed 50 characters")
   private String businessName;

    @NotBlank(message = "Location should not be blank or empty" )
    @Size(max = 50,message = "location should not exceed 50 characters")
    private String location;

    @NotBlank(message =  "City should not be blank or empty")
    @Size( max = 50,message =  "City should not exceed 50 characters")
    private String city;

}
