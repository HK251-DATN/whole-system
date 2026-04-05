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
import microservice.base_source.domain.entity.ProductGeneral;
import microservice.base_source.domain.use_case.ProductGeneralUseCase;
import microservice.base_source.presentation.request.ProductGeneralRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/product-generals")
@RequiredArgsConstructor
public class ProductGeneralController {
    @Autowired
	private final ProductGeneralUseCase productGeneralUseCase;

    @PostMapping
    public ApiResponse<ProductGeneral> create(@Valid @RequestBody ProductGeneralRequest req) {
        ProductGeneral created = productGeneralUseCase.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create product general success", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductGeneral> getById(@PathVariable Long id) {
        ProductGeneral p = productGeneralUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get product general success", p);
    }

    @GetMapping
    public ApiResponse<List<ProductGeneral>> getAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        if (productGeneralUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No product generals found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all product generals success", productGeneralUseCase.getAll(page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductGeneral> update(@Valid @RequestBody ProductGeneralRequest req) {
        ProductGeneral toUpdate = req.toEntity();
        ProductGeneral updated = productGeneralUseCase.update(req.getCategoryId(), toUpdate);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productGeneralUseCase.delete(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
    }
}
