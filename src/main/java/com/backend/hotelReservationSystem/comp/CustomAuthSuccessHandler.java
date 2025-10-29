package com.backend.hotelReservationSystem.comp;

import com.backend.hotelReservationSystem.enums.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_"+Role.ADMIN)) {
            response.sendRedirect(request.getContextPath()+"/admin/resource/dashboard");
        } else if (roles.contains("ROLE_"+Role.BUSINESS)) {
            response.sendRedirect(request.getContextPath()+"/business/resource/dashboard");
        } else {
            response.sendRedirect(request.getContextPath()+"/user/resource/dashboard");
        }
    }
}
