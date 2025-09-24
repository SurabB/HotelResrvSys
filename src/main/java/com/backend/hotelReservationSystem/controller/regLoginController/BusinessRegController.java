package com.backend.hotelReservationSystem.controller.regLoginController;

import com.backend.hotelReservationSystem.dto.commonDto.EmailDto;
import com.backend.hotelReservationSystem.enums.Role;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.exceptionClasses.RegistrationFailedException;
import com.backend.hotelReservationSystem.service.regServ.RegistrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/business/resource")
@PreAuthorize("hasRole('BUSINESS')")
@AllArgsConstructor
public class BusinessRegController {
    private final RegistrationService registrationService;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/reg")
    public String businessReg(){
        return "businessService/businessRegEmail";
    }
    @PreAuthorize("isAnonymous()")
    @PostMapping("/reg")
    public String registerBusiness(@Valid @ModelAttribute EmailDto emaildto,
                                   BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
            throw new CustomMethodArgFailedException("redirect:/business/resource/reg", bindingResult);
        }
        try {
            registrationService.userTokenRegistration(emaildto.getEmail(), Role.BUSINESS);
            redirectAttributes.addFlashAttribute("success", "If the entered email is not already verified , an email has been sent with instructions of how to verify email");
            return "redirect:/common/resource/verifyEmail";
        }
        catch(Exception e){
            throw new RegistrationFailedException("redirect:/common/resource/reg",e);
        }
    }
    @GetMapping("/dashboard")
    public String dashboardPage(){
        return "businessService/BusinessDashboard";
    }
}
