package com.backend.hotelReservationSystem.exceptionClasses;

import lombok.Getter;
import org.springframework.validation.BindingResult;
@Getter
public class CustomMethodArgFailedException extends RuntimeException {
    private final BindingResult bindingResult;
    private final String redirectionUrl;
    public CustomMethodArgFailedException(String redirectionUrl, BindingResult bindingResult) {
        this.redirectionUrl = redirectionUrl;
    this.bindingResult = bindingResult;
    }
}
