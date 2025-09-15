package com.backend.hotelReservationSystem.exceptionClasses;

import lombok.Getter;
@Getter
public class TokenInvalidException extends RuntimeException {

  public TokenInvalidException(String message){
      super(message);


  }
}
