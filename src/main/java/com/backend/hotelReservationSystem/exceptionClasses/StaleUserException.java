package com.backend.hotelReservationSystem.exceptionClasses;

public class StaleUserException extends IllegalCallerException {
    public StaleUserException(String s) {
        super(s);
    }
}
