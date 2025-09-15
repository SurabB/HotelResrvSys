package com.backend.hotelReservationSystem.exceptionClasses;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class EmailNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus;
    public EmailNotFoundException(String message,HttpStatus httpStatus) {
        super(message);
        this.httpStatus=httpStatus;
    }
}
