package com.backend.hotelReservationSystem.dto.commonDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VerificationTokenAcceptor {
    @NotBlank(message = "VerificationToken should not be blank")
   @Size(min = 5,message = "Verification token is invalid")
   private  String verificationToken;

    @NotBlank(message="Password must not be blank")
    @Size(min=5,max=25,message = "Password must be between 5 and 25 characters")
   private String password;
}
