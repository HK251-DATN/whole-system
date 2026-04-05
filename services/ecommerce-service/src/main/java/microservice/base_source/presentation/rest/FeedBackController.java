package microservice.base_source.presentation.rest;

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
import microservice.base_source.domain.entity.FeedBack;
import microservice.base_source.domain.use_case.FeedBackUseCase;
import microservice.base_source.persistence.dto.FeedBackDTO;
import microservice.base_source.presentation.request.FeedBackRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/feed-backs")
@RequiredArgsConstructor
public class FeedBackController {
    @Autowired
	private FeedBackUseCase feedBackUseCase;

    @PostMapping
    public ApiResponse<FeedBack> create(@Valid @RequestBody FeedBackRequest req) {
        FeedBack created = feedBackUseCase.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success" , created);
    }

    @GetMapping("/{id}")
    public ApiResponse<FeedBack> getById(@PathVariable Long id) {
        FeedBack opt = feedBackUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get feed back success", opt);
    }

    @GetMapping
    public ApiResponse<List<FeedBack>> getAll(
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (feedBackUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No feed backs found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all feed backs success", feedBackUseCase.getAll(page, size));
    }

    @GetMapping("/{batchId}")
    public ApiResponse<List<FeedBackDTO>> search(
        @PathVariable(name = "batchId") Long batchId, 
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (feedBackUseCase.getByBatchId(batchId, page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No feed backs found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search feed backs success", feedBackUseCase.getByBatchId(batchId, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<FeedBack> update(@PathVariable Long id, @Valid @RequestBody FeedBackRequest req) {
        FeedBack toUpdate = req.toEntity();
        FeedBack updated = feedBackUseCase.update(id, toUpdate);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
	}

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
		feedBackUseCase.delete(id);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
	}
}
