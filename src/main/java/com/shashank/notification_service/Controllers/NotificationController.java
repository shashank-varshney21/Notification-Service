package com.shashank.notification_service.Controllers;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.DTO.RequestObserverDto;
import com.shashank.notification_service.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/create")
        public NotiMessageDto createSimpleNotification(@RequestBody String text) {
            return notificationService.createSimpleNotification(text);
    }

    @PostMapping("/create/timestamp")
        public NotiMessageDto createTimestampNotification(@RequestBody String text) {
            return notificationService.createTimestampNotification(text);
    }

    @PostMapping("/send")
        public ResponseEntity<String> sendNotification(@RequestBody NotiMessageDto notiMessageDto) {
            return notificationService.sendNotification(notiMessageDto);
    }

    @PostMapping("/create/observer")
        public ResponseEntity<String> createObserver(@RequestBody RequestObserverDto requestObserverDto) {
            return notificationService.createObserver(requestObserverDto);
    }
}
