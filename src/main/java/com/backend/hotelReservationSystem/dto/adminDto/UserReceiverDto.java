package com.backend.hotelReservationSystem.dto.adminDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserReceiverDto {
    String email;
    String role;
}
