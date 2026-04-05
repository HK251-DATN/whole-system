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
import microservice.base_source.domain.entity.ProductDetail;
import microservice.base_source.domain.use_case.ProductDetailUseCase;
import microservice.base_source.presentation.request.ProductDetailRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/product-details")
@RequiredArgsConstructor
public class ProductDetailController {
    @Autowired
	private final ProductDetailUseCase productDetailUseCase;

    @PostMapping
    public ApiResponse<ProductDetail> create(@Valid @RequestBody ProductDetailRequest req) {
        ProductDetail created = productDetailUseCase.create(req.toEntity());
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create product detail success", created);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetail> getById(@PathVariable Long id) {
        ProductDetail pd = productDetailUseCase.get(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get product detail success", pd);
    }

    @GetMapping
    public ApiResponse<List<ProductDetail>> getAll(
		@RequestParam(defaultValue = "0") Integer page, 
		@RequestParam(defaultValue = "20") Integer size) {
        if (productDetailUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No product details found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all product details success", productDetailUseCase.getAll(page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDetail> update(@PathVariable Long id, @Valid @RequestBody ProductDetailRequest req) {
        ProductDetail toUpdate = req.toEntity();
        ProductDetail updated = productDetailUseCase.update(id, toUpdate);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productDetailUseCase.delete(id);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
    }
}
