package edu.hcmut.datn.back_office_service.dto.request.event;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.dao.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {
    private String cronExp;
    private LocalDateTime endTime;
    private Boolean isActive;

    public Event toEntity() {
        return new Event(cronExp, endTime, isActive);
    }
}
