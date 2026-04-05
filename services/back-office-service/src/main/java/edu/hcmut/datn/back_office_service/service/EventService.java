package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import org.springframework.data.util.Pair;

import edu.hcmut.datn.back_office_service.dao.Event;
import edu.hcmut.datn.back_office_service.dao.ProductRequest;
import edu.hcmut.datn.back_office_service.dao.SaleEvent;

public interface EventService {

    Event create(Event event);

    Event read(Long eventId);

    List<Event> readAll(Integer pageNum, Integer pageSize);

    Event update(Long eventId, Event event);

    void delete(Long eventId);

    Pair<Event, SaleEvent> createLoopableSaleEvent(Event event, SaleEvent saleEvent);

    Pair<Event, ProductRequest> createLoopableProductRequest(Event event, ProductRequest request);
}
