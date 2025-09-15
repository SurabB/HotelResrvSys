package com.backend.hotelReservationSystem.dto.commonDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailDto {
    @NotBlank(message = "Email should not be blank or empty")
    @Email(message = "Provided email doesn't match standard email pattern.")
    @Size(max=50,message = "Email should not exceed 50 characters")
     private String email;
}
