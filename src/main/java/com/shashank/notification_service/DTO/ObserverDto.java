package com.shashank.notification_service.DTO;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class ObserverDto {
    @Id
    private Long id;
    private String name;
    private String Email;
    private Long phone;
}
