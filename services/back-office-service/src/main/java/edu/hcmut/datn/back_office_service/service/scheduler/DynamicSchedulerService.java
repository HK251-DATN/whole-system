package edu.hcmut.datn.back_office_service.service.scheduler;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.Event;
import edu.hcmut.datn.back_office_service.repository.EventRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DynamicSchedulerService {

    private final TaskScheduler taskScheduler;

    private final EventRepository eventRepository;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        List<Event> events = eventRepository.findAll();

        events.forEach((event) -> scheduleEvent(event));
    }

    public void scheduleEvent(Event event) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> System.out.println(LocalTime.now().toString() + ": " + event.getEventId()),
                new CronTrigger(event.getCronExp())
        );

        scheduledTasks.put(event.getEventId(), future);
    }
}
