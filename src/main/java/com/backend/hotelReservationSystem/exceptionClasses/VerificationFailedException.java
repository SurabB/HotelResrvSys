package com.backend.hotelReservationSystem.exceptionClasses;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class VerificationFailedException extends RuntimeException
{
    private final HttpStatus httpStatus;
    public VerificationFailedException(String message, HttpStatus httpStatus,Throwable cause)
    {
        super(message,cause);
        this.httpStatus=httpStatus;

    }
}
