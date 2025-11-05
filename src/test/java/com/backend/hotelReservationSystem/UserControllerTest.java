package com.backend.hotelReservationSystem;
import com.backend.hotelReservationSystem.controller.actualcontrollers.UserController;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.exceptionClasses.BookingCancellationException;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.service.actualservice.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepo userRepo ;

    @Test
    //providing valid credentials
    void bookRoom() throws Exception {
        Business business = Business.builder().businessId(1L).build();

        mockMvc.perform(post("/user/service/bookRoom")
                        .with(user("john").roles("USER"))
                        .with(csrf())
                        .param("roomNumber", "2")
                        .param("checkInTime", "2025-11-12T17:20:00")
                        .param("checkoutTime", "2025-11-12T19:20:00")
                        .param("userProvidePrice", "4.00")
                        .sessionAttr("business", business))
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    assertNull(resolvedException);
                }
                )
                .andExpect(status().isFound());
    }
    @Test
    //providing past checkIn,checkout date
    void bookRoomTest2() throws Exception {
        Business business = Business.builder().businessId(1L).build();

        mockMvc.perform(post("/user/service/bookRoom")
                        .with(user("john").roles("USER"))
                        .with(csrf())
                        .param("roomNumber", "1")
                        .param("checkInTime", "2025-05-12T17:20:00")
                        .param("checkoutTime", "2025-09-12T19:20:00")
                        .param("userProvidePrice", "4.00")
                        .sessionAttr("business", business))
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    assertNotNull(resolvedException);
                    assertTrue(resolvedException instanceof CustomMethodArgFailedException);
                });
    }
}
