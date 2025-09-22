package com.backend.hotelReservationSystem;

import com.backend.hotelReservationSystem.config.SecurityConfig;
import com.backend.hotelReservationSystem.dto.userServiceDto.BookRoomDto;
import com.backend.hotelReservationSystem.service.regServ.CustomMailSender;
import com.backend.hotelReservationSystem.utils.BookingPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class HotelReservationSystemApplicationTests {

    @Test
  void contextLoads(){
        BookRoomDto bookRoomDto=new BookRoomDto( LocalDate.now(),"12:00am","11:59pm");
        BookingPolicy.BookingTime time = BookingPolicy.getTime(bookRoomDto);
        System.out.println(time.getCheckInDate()+","+time.getCheckoutDate());
    }


}
