package com.backend.hotelReservationSystem.config;

import com.backend.hotelReservationSystem.entity.User;
import com.backend.hotelReservationSystem.repo.UserRepo;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.Optional;

@Configuration
@EnableScheduling
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  AuthenticationSuccessHandler customSuccessHandler,
                                                  AuthenticationFailureHandler customLoginErrorHandler,
                                                  LogoutSuccessHandler customLogoutSuccessHandler) throws Exception {
       http
               .authorizeHttpRequests(request->
                   request
                           .requestMatchers("/css/**").permitAll()
                           .requestMatchers("/user/resource/reg","/business/resource/reg").permitAll()
                           .requestMatchers("/common/resource/**").permitAll()
                           .anyRequest().authenticated()
               )
               .formLogin(loginForm->
                   loginForm
                           .loginPage("/common/resource/login")
                           .loginProcessingUrl("/login")
                           .successHandler(customSuccessHandler)
                           .failureHandler(customLoginErrorHandler)

               )
               .logout(logout->
                   logout
                           .logoutUrl("/logoutUrl")
                           .logoutSuccessHandler(customLogoutSuccessHandler)

               )
               .sessionManagement(session->{
                   session
                           .maximumSessions(-1)
                           .sessionRegistry(sessionRegistry())
                           .expiredUrl("/common/resource/login");

               });
       return http.build();
   }
   @Bean
   public UserDetailsService userDetailsService(UserRepo userRepo){
        return userEmail->{
            Optional<User> userByEmail = userRepo.findUserByEmail(userEmail);
            User user = userByEmail.orElseThrow(() -> new UsernameNotFoundException("provided user email is invalid"));
           return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().toString())
                    .accountLocked(!user.getIsActive())
                    .build();
        };

   }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    @Bean
    public static ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }




}
