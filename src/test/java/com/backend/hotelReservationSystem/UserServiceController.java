package com.backend.hotelReservationSystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceController {
    @Autowired
    private MockMvc mockMvc;
    @Test
    public void bookRoom() throws Exception {
//        mockMvc.perform(post("/user/service/bookRoom")
//                        .param("roomNumber", "1")
//                        .param("checkInDate", "2025-09-22T17:20:00")
//                        .param("checkoutTime","2025-09-22T19:20:00"))
//                .andExpect(status().isOk())
//                .andDo(print());
    }

}
