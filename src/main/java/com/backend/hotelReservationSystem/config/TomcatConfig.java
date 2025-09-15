package com.backend.hotelReservationSystem.config;

import org.apache.catalina.session.StandardManager;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class TomcatConfig {

   // @Bean
    public TomcatContextCustomizer disableSessionPersistence() {
        return context -> {
         StandardManager manager = new StandardManager();
            manager.setPathname(null);
            context.setManager(manager);
        };
    }



    }

