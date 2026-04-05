package edu.hcmut.datn.back_office_service.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hcmut.datn.back_office_service.dao.Event;
import edu.hcmut.datn.back_office_service.dao.ProductRequest;
import edu.hcmut.datn.back_office_service.dao.SaleEvent;
import edu.hcmut.datn.back_office_service.dto.request.event.EventCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.event.EventUpdateRequest;
import edu.hcmut.datn.back_office_service.dto.request.event.ProductRequestCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.event.SaleEventCreateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.EventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/event")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<Event>> create(@RequestBody EventCreateRequest request) {
        try {
            Event newEvent = eventService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create event successfully", newEvent));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Event>> read(@PathVariable Long eventId) {
        try {
            Event event = eventService.read(eventId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read event successfully", event));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        List<Event> events = eventService.readAll(pageNum, pageSize);

        if (events.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No Event Exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all events successfully", events));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Event>> update(@PathVariable Long eventId,
            @RequestBody EventUpdateRequest request) {
        try {
            Event updated = eventService.update(eventId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update event successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long eventId) {
        try {
            eventService.delete(eventId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete event successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/sale-event")
    public ResponseEntity<ApiResponse<Pair<Event, SaleEvent>>> createSaleEventEvent(@RequestBody SaleEventCreateRequest request) {
        Event newEvent = request.toEntity();

        SaleEvent newSaleEvent = request.toSaleEventEntity();

        try {
            Pair<Event, SaleEvent> pair = eventService.createLoopableSaleEvent(newEvent, newSaleEvent);

            return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create loopable sale event successfully", pair));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/product-request")
    public ResponseEntity<ApiResponse<Pair<Event, ProductRequest>>> createProductRequestEvent(@RequestBody ProductRequestCreateRequest request) {
        Event newEvent = request.toEntity();

        ProductRequest newRequest = request.toProductRequestEntity();

        try {
            Pair<Event, ProductRequest> pair = eventService.createLoopableProductRequest(newEvent, newRequest);

            return ResponseEntity.ok().body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create loopable sale event successfully", pair));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
