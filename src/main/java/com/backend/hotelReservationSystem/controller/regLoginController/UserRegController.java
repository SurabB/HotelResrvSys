package com.backend.hotelReservationSystem.controller.regLoginController;

import com.backend.hotelReservationSystem.dto.commonDto.EmailDto;
import com.backend.hotelReservationSystem.entity.Business;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.exceptionClasses.RegistrationFailedException;
import com.backend.hotelReservationSystem.service.regServ.RegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;




@Controller
@RequestMapping("/user/resource")
@AllArgsConstructor
@Slf4j
public class UserRegController {
    private final RegistrationService registrationService;
   @PreAuthorize("isAnonymous()")
    @PostMapping("/reg")
    public String register(@Valid @ModelAttribute EmailDto emailDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
       if(bindingResult.hasErrors()) {
           throw new CustomMethodArgFailedException("redirect:/common/resource/reg", bindingResult);
       }
       try {
           registrationService.userTokenRegistration(emailDto.getEmail(), Role.USER);
           redirectAttributes.addFlashAttribute("success", "If the entered email is not already verified , an email has been sent with instructions of how to verify email");
           return "redirect:/common/resource/verifyEmail";
       } catch (Exception e) {
           throw new RegistrationFailedException("redirect:/common/resource/reg",e);
       }
   }



@PreAuthorize("hasRole('USER')")
    @GetMapping("/dashboard")
    public String dashboardPage(@SessionAttribute(value = "business",required = false)Business business, HttpSession session)
{
    if(business!=null){
        session.removeAttribute("business");
    }
        return "userService/userDashboard";
    }

}
