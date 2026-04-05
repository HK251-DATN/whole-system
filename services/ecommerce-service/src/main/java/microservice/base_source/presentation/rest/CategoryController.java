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
import microservice.base_source.domain.entity.Category;
import microservice.base_source.domain.use_case.CategoryUseCase;
import microservice.base_source.presentation.request.CategoryRequest;
import microservice.base_source.presentation.response.category.CategoryResponse;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    @Autowired
	private CategoryUseCase categoryUseCase;
    
    /**
     * NOTE: Category creation is now handled by back-office service.
     * Categories are created via Kafka events from back-office.
     * This endpoint has been removed.
     */
//    @PostMapping
//    public ApiResponse<Category> create(@Valid @RequestBody CategoryRequest req) {
//        Category created = categoryUseCase.create(req.toEntity());
//        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success" , created);
//    }

    @GetMapping("/{id}")
    public ApiResponse<Category> getById(@PathVariable Long id) {
        Category opt = categoryUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get category success", opt);
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll(
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (categoryUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No categories found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all categories success", categoryUseCase.getAll(page, size));
    }

    @GetMapping("/search")
    public ApiResponse<List<Category>> search(
        @RequestParam(defaultValue = "") String searchString, 
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (categoryUseCase.search(searchString, page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No categories found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Search categories success", categoryUseCase.search(searchString, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        Category toUpdate = req.toEntity();
        toUpdate.setCategoryId(id);
        Category updated = categoryUseCase.update(id, toUpdate);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
	}

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
		categoryUseCase.delete(id);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
	}
}
