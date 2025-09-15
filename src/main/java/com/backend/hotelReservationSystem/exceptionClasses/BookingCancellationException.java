package com.backend.hotelReservationSystem.exceptionClasses;

public class BookingCancellationException extends RuntimeException {
    public BookingCancellationException(String s) {
        super(s);
    }
}
