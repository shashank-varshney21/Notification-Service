package com.shashank.notification_service.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Value("${kafka.topic.notification}")
    private String kafkaTopic;

    @Bean
    public NewTopic notificationTopic() {
        return new NewTopic(kafkaTopic, 3, (short) 1);
    }
}
