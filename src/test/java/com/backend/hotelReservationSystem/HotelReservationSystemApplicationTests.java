package com.backend.hotelReservationSystem;

import com.backend.hotelReservationSystem.config.SecurityConfig;
import com.backend.hotelReservationSystem.service.regServ.CustomMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HotelReservationSystemApplicationTests {
    @Autowired
    SecurityConfig custom;
    @Test
  void contextLoads() {
        System.out.println(custom);
  }


}
