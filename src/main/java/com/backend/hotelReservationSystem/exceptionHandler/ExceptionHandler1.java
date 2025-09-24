package com.backend.hotelReservationSystem.exceptionHandler;

import com.backend.hotelReservationSystem.exceptionClasses.CustomMethodArgFailedException;
import com.backend.hotelReservationSystem.exceptionClasses.RegistrationFailedException;
import com.backend.hotelReservationSystem.exceptionClasses.StaleUserException;
import com.backend.hotelReservationSystem.exceptionClasses.VerificationFailedException;
import com.backend.hotelReservationSystem.service.regServ.InvalidateSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class ExceptionHandler1 {
    private final InvalidateSession invalidateSession;

    @ExceptionHandler(VerificationFailedException.class)
    public String handleVerificationFailedException(VerificationFailedException ex,RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("failure", ex.getMessage());
        return "redirect:/common/resource/verifyEmail";
    }
    @ExceptionHandler(CustomMethodArgFailedException.class)
    public String handleMethodArgumentNotValid(CustomMethodArgFailedException e, RedirectAttributes redirectAttributes) {
        List<String> errors = e.getBindingResult().getAllErrors().stream().map(err -> err.getDefaultMessage()).toList();
        redirectAttributes.addFlashAttribute("fieldErrors", errors);
        return e.getRedirectionUrl();

    }

    @ExceptionHandler(RegistrationFailedException.class)
    public String handleRegistrationFailedException(RegistrationFailedException ex,RedirectAttributes redirectAttributes) {
        String message=switch(ex.getCause()){
            case MailException e->"Something went wrong while sending verification mail. Please try again";
            default->"Something went wrong on server side";
        };
        redirectAttributes.addFlashAttribute("failure",message);
        return ex.getRedirectionUrl();
    }
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("authError", "Access denied! You don’t have permission.");
        return "redirect:/common/resource/errorPage";
    }
@ExceptionHandler(StaleUserException.class)
    public String handleStaleUserException(RedirectAttributes redirectAttributes, Principal principal) {

        //gets stale user from securityContext
    String username = principal.getName();

    //invalidates session based on username(email)
    invalidateSession.expireUserSessionsByEmail(username);

    //removes current context
    SecurityContextHolder.clearContext();
    log.warn("Stale user session invalidated for username: {}", username);
    redirectAttributes.addFlashAttribute("login_failure", "Your account has been deleted. Contact customer support for more information.");
    return "redirect:/common/resource/login";
}

}
