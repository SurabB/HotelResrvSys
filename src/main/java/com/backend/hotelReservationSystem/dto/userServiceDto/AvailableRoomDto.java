package com.backend.hotelReservationSystem.dto.userServiceDto;

import com.backend.hotelReservationSystem.entity.Room;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;



    @Getter
    @Setter
    public class AvailableRoomDto {
        private Long roomNumber;
       private BigDecimal pricePerHour;
        private String roomType;
    public AvailableRoomDto(Room room){
        this.roomNumber=room.getRoomNumber();
        this.pricePerHour=room.getPricePerHour();
        this.roomType=room.getRoomType();
    }
    public AvailableRoomDto getRoom(){
        return this;
    }

    }

