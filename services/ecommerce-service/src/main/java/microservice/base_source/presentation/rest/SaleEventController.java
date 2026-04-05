package microservice.base_source.presentation.rest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.SaleEvent;
import microservice.base_source.domain.use_case.SaleEventUseCase;
import microservice.base_source.presentation.request.SaleEventRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/sale-events")
@RequiredArgsConstructor
public class SaleEventController {
    @Autowired
	private SaleEventUseCase saleEventUseCase;

    @PostMapping
    public ApiResponse<SaleEvent> create(@Valid @RequestBody SaleEventRequest req) {
        SaleEvent created = saleEventUseCase.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success" , created);
    }

    @GetMapping("/{id}")
    public ApiResponse<SaleEvent> getById(@PathVariable Long id) {
        SaleEvent opt = saleEventUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get sale event success", opt);
    }

    @GetMapping
    public ApiResponse<List<SaleEvent>> getAll(
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (saleEventUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No sale events found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all sale events success", saleEventUseCase.getAll(page, size));
    }

    @GetMapping("/search")
    public ApiResponse<List<SaleEvent>> search(
        @RequestParam(defaultValue = "") String searchString, 
		@RequestParam(defaultValue = "") String activeYn, 
        @RequestParam(defaultValue = "") String enableYn,
		@RequestParam(required = false) LocalTime beginTime, 
        @RequestParam(required = false) LocalTime endTime,
		@RequestParam(required = false) LocalDateTime beginDate, 
        @RequestParam(required = false) LocalDateTime endDate,
		@RequestParam(defaultValue = "1") int page, 
        @RequestParam(defaultValue = "20") int size) {
        if (saleEventUseCase.searchEvents(searchString, activeYn, enableYn, beginTime, endTime, beginDate, endDate, page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No sale events found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search sale events success", saleEventUseCase.searchEvents(searchString, activeYn, enableYn, beginTime, endTime, beginDate, endDate, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<SaleEvent> update(@PathVariable Long id, @Valid @RequestBody SaleEventRequest req) {
        SaleEvent toUpdate = req.toEntity();
        SaleEvent updated = saleEventUseCase.update(id, toUpdate);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
	}

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
		saleEventUseCase.delete(id);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
	}
}
