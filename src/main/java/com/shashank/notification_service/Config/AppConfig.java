package com.shashank.notification_service.Config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

public class AppConfig {
    @Bean
        public ModelMapper getMapper() {
            return new ModelMapper();
    }
}
