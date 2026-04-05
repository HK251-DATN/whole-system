package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.Event;
import edu.hcmut.datn.back_office_service.dao.ProductRequest;
import edu.hcmut.datn.back_office_service.dao.SaleEvent;
import edu.hcmut.datn.back_office_service.exception.event.EventNotFoundException;
import edu.hcmut.datn.back_office_service.repository.EventRepository;
import edu.hcmut.datn.back_office_service.repository.ProductRequestRepository;
import edu.hcmut.datn.back_office_service.repository.SaleEventRepository;
import edu.hcmut.datn.back_office_service.service.EventService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    private final SaleEventRepository saleEventRepository;

    private final ProductRequestRepository productRequestRepository;

    @Override
    public Event create(Event event) {
        try {
            return eventRepository.save(event);
        } catch (Exception e) {
            log.info("Error message: {}", e.getMessage());

            return null;
        }
    }

    @Override
    public Event read(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event Not Found"));
    }

    @Override
    public List<Event> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<Event> events = eventRepository.findAll(pageable);

        return events.toList();
    }

    @Override
    public Event update(Long eventId, Event event) {
        Event cur = read(eventId);

        if (event.getCronExp() != null) {
            cur.setCronExp(event.getCronExp());
        }

        if (event.getEndTime() != null) {
            cur.setEndTime(event.getEndTime());
        }

        if (event.getIsActive() != null) {
            cur.setIsActive(event.getIsActive());
        }

        return eventRepository.save(cur);
    }

    @Override
    public void delete(Long eventId) {
        eventRepository.delete(read(eventId));
    }

    @Override
    @Transactional
    public Pair<Event, SaleEvent> createLoopableSaleEvent(Event event, SaleEvent saleEvent) {
        Event newEvent = create(event);

        saleEvent.setEventId(newEvent.getEventId());
        SaleEvent newSaleEvent = saleEventRepository.save(saleEvent);

        return Pair.of(newEvent, newSaleEvent);
    }

    @Override
    @Transactional
    public Pair<Event, ProductRequest> createLoopableProductRequest(Event event, ProductRequest request) {
        Event newEvent = create(event);

        request.setEventId(newEvent.getEventId());
        ProductRequest newRequest = productRequestRepository.save(request);

        return Pair.of(newEvent, newRequest);
    }
}
