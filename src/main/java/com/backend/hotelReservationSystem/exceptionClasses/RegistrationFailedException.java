package com.backend.hotelReservationSystem.exceptionClasses;

import lombok.Getter;
@Getter
public class RegistrationFailedException extends RuntimeException {
    String redirectionUrl;

    public RegistrationFailedException(String redirectionUrl,Throwable cause) {
        super(cause);
        this.redirectionUrl = redirectionUrl;
    }
}
