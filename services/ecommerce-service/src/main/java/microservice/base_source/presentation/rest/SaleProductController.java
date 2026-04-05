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
import microservice.base_source.domain.entity.SaleProduct;
import microservice.base_source.domain.use_case.SaleProductUseCase;
import microservice.base_source.presentation.request.SaleProductRequest;
import microservice.base_source.presentation.response.global.ApiResponse;

@RestController
@RequestMapping("/api/sale-products")
@RequiredArgsConstructor
public class SaleProductController {
    @Autowired
	private SaleProductUseCase saleProductUseCase;

    @PostMapping
    public ApiResponse<SaleProduct> create(@Valid @RequestBody SaleProductRequest req) {
        SaleProduct created = saleProductUseCase.create(req.toEntity());
        System.out.println("THIS APUI RUNNING");
        return ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Create success" , created);
    }

    @GetMapping("/{saleEventId}/{batchId}")
    public ApiResponse<SaleProduct> getById(@PathVariable(name = "saleEventId") Long saleEventid, @PathVariable(name = "batchId") String batchId) {
        SaleProduct opt = saleProductUseCase.get(saleEventid, batchId);
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get saleProduct success", opt);
    }

    @GetMapping
    public ApiResponse<List<SaleProduct>> getAll(
        @RequestParam(defaultValue = "1") Integer page, 
        @RequestParam(defaultValue = "20") Integer size) {
        if (saleProductUseCase.getAll(page, size).isEmpty()) {
            return ApiResponse.SKIP_AS_GOOD(HttpStatus.NO_CONTENT.toString(), "No sale products found", null);
        }
        return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all sale products success", saleProductUseCase.getAll(page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<SaleProduct> update(@PathVariable Long id, @Valid @RequestBody SaleProductRequest req) {
        SaleProduct toUpdate = req.toEntity();
        SaleProduct updated = saleProductUseCase.update(req.getSaleEventId(), req.getBatchId(), toUpdate);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update success", updated);
	}

    @DeleteMapping("/{saleEventId}/{batchId}")
    public ApiResponse<Void> delete(@PathVariable(name = "saleEventId") Long saleEventid, @PathVariable(name = "batchId") String batchId) {
		saleProductUseCase.delete(saleEventid, batchId);
		return ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete success", null);
	}
}
