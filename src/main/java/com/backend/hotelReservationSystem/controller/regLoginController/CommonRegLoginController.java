package com.backend.hotelReservationSystem.controller.regLoginController;

import com.backend.hotelReservationSystem.dto.commonDto.EmailDto;
import com.backend.hotelReservationSystem.enums.MailStatus;
import com.backend.hotelReservationSystem.dto.commonDto.VerificationTokenAcceptor;
import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.exceptionClasses.TokenInvalidException;
import com.backend.hotelReservationSystem.exceptionClasses.VerificationFailedException;
import com.backend.hotelReservationSystem.repo.UserRepo;
import com.backend.hotelReservationSystem.service.regServ.InvalidateSession;
import com.backend.hotelReservationSystem.service.regServ.RegistrationService;
import com.backend.hotelReservationSystem.service.regServ.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
@Slf4j
@Controller
@RequestMapping("/common/resource")
@AllArgsConstructor
public class CommonRegLoginController {
    private final TokenService tokenService;
    private final UserRepo userRepo;
    private final RegistrationService registrationService;
    private final InvalidateSession invalidateSession;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/reg")
    public String register() {
        return "userService/registration";
    }

    @PreAuthorize("isAnonymous()|| hasRole('ADMIN')")
    @GetMapping("/verifyEmail")
    public String verifyEmail(){
        return "commonService/emailVerification";
    }


    @GetMapping("/login")
    public String loginPage(HttpServletRequest request,Model model) {
        Object loginFailureMsg=request.getSession().getAttribute("login_failure_msg");
        Object logoutSuccessMsg=request.getSession().getAttribute("logout_success_msg");
        if(logoutSuccessMsg!=null){
            model.addAttribute("logout_success",logoutSuccessMsg);
            request.getSession().removeAttribute("logout_success_msg");
        }
        if(loginFailureMsg!=null){
            model.addAttribute("login_failure",loginFailureMsg);
            request.getSession().removeAttribute("login_failure_msg");
        }

        return "commonService/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/logout")
    public String logoutPage() {

        return "commonService/logout";
    }
   @PreAuthorize("isAnonymous()|| hasRole('ADMIN')")
    @PostMapping("/verifyEmail")
    public String verifyEmail(@Valid @ModelAttribute VerificationTokenAcceptor verificationTokenAcceptor, BindingResult bindingResult, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails){
        if(bindingResult.hasErrors()) {
            throw new CustomMethodArgFailedException("redirect:/common/resource/verifyEmail", bindingResult);
        }
        try {
            User user = tokenService.verifyEmailAndSetPassword(verificationTokenAcceptor);
            invalidateSession.expireUserSessionsByEmail(user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Verification successful");
            if(userDetails!=null){
               return "redirect:/admin/resource/reg";
            }
                return "redirect:/common/resource/login";
        }
        catch (TokenInvalidException e) {
            log.error("user verification failed {}",e.getMessage());
            throw new VerificationFailedException( "Provided token is either invalid or expired",HttpStatus.BAD_REQUEST,e);
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("failure","something went wrong while verifying email");
            return "redirect:/common/resource/verifyEmail";
        }
    }
    @PreAuthorize("isAnonymous()")
    @GetMapping("/forgotPasswordEmail")
    public String forgotPassword(){
        return "commonService/passwordResetEmail";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/forgotPasswordEmail")
    public String forgotPassword(@Valid @ModelAttribute EmailDto emailDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()) {
            throw new CustomMethodArgFailedException("redirect:/common/resource/forgotPasswordEmail", bindingResult);
        }
        try {
            Optional<User> userFromDb = userRepo.findUserByEmail(emailDto.getEmail());
            String verificationMsg = "If the entered email is associated with an account and is verified, an email has been sent with password reset token to reset the password";
            userFromDb.filter(user -> user.getIsEmailVerified()
                            && (user.getMailToken() == null
                            || user.getMailToken().getExpiryDate().isBefore(LocalDateTime.now())
                            || user.getMailToken().getMailStatus() == MailStatus.PENDING))
                    .ifPresent(user -> registrationService.userTokenRegistration(user));
            redirectAttributes.addFlashAttribute("success", verificationMsg);
            return "redirect:/common/resource/verifyEmail";
        }
        catch(Exception e){
            redirectAttributes.addFlashAttribute("failure","something went wrong while verifying email");
            return "redirect:/common/resource/forgotPasswordEmail";
        }
    }

    @GetMapping("/dashboard")
    public String commonDashboard() {
        return "commonService/commonDashboard";

    }

    @GetMapping("/errorPage")
    public String errorPage(){
        return "error";
    }


    @GetMapping("/roleBasedDashboard")
    public  String roleBasedDashboard(@AuthenticationPrincipal UserDetails userDetails,RedirectAttributes redirectAttributes){
        if (userDetails==null){
            redirectAttributes.addFlashAttribute("failure","You need to login first");
            return "redirect:/common/resource/dashboard";
        }
        String userRole = userDetails.getAuthorities().toString();
        String redirectPoint=switch (userRole){
           case "[ROLE_USER]"-> "redirect:/user/resource/dashboard";
           case "[ROLE_BUSINESS]"-> "redirect:/business/resource/dashboard";
           case "[ROLE_ADMIN]"-> "redirect:/admin/resource/dashboard";
           default -> {
               redirectAttributes.addFlashAttribute("failure","You need to login first");
               yield "redirect:/common/resource/dashboard";
           }
       };
       return redirectPoint;

    }


}