package com.backend.hotelReservationSystem.exceptionClasses;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String msg) {
        super(msg);
    }
}
