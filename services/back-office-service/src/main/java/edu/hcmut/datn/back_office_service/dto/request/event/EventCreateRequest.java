package edu.hcmut.datn.back_office_service.dto.request.event;

import java.time.LocalDateTime;

import edu.hcmut.datn.back_office_service.common.enums.EventType;
import edu.hcmut.datn.back_office_service.dao.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class EventCreateRequest {

    private EventType eventType;
    private String cronExp;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private LocalDateTime lastTrigger;
    private LocalDateTime nextTrigger;
    private Long createdBy;

    public Event toEntity() {

        Event event = new Event(eventType, cronExp, beginTime, endTime, isActive, lastTrigger, nextTrigger, createdBy);

        log.info(event.toString());

        return event;
    }
}
