package com.backend.hotelReservationSystem.service.actualservice;

import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("customAuth")
@AllArgsConstructor
// A Custom authentication class
public class CustomAuthService {
    private final UserRepo userRepo;

    //used in controllers (@PreAuthorize(@customAuth.isBusinessReg))
    public boolean isBusinessReg(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userByEmail = userRepo.findUserByEmail(authentication.getName());
        //returns true if business is present and is active else returns false
        return  userByEmail.filter(user -> user.getIsActive() && user.getBusiness() != null).isPresent();

    }
}
