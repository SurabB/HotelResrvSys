package com.backend.hotelReservationSystem.exceptionClasses;

public class StaleUserException extends IllegalStateException {
    public StaleUserException(String s) {
        super(s);
    }
}
