package com.backend.hotelReservationSystem.exceptionClasses;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String s) {
        super(s);
    }
}
