package edu.hcmut.datn.back_office_service.controller;

import edu.hcmut.datn.back_office_service.dao.Category;
import edu.hcmut.datn.back_office_service.dao.SubSubcategory;
import edu.hcmut.datn.back_office_service.dto.request.CategoryCreateRequest;
import edu.hcmut.datn.back_office_service.dto.request.SubSubcategoryCreateRequest;
import edu.hcmut.datn.back_office_service.dto.response.ApiResponse;
import edu.hcmut.datn.back_office_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    // Category endpoints
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @RequestBody CategoryCreateRequest request) {
        try {
            Category category = categoryService.create(request.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.SUCCESS(HttpStatus.CREATED.toString(),
                            "Category created successfully", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> getCategory(@PathVariable Long categoryId) {
        try {
            Category category = categoryService.read(categoryId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Category retrieved successfully", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.readAll();
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                        "Categories retrieved successfully", categories));
    }
    
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<ApiResponse<List<Category>>> getSubcategories(
            @PathVariable Long parentId) {
        List<Category> subcategories = categoryService.readSubcategories(parentId);
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                        "Subcategories retrieved successfully", subcategories));
    }
    
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryCreateRequest request) {
        try {
            Category updated = categoryService.update(categoryId, request.toEntity());
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Category updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.delete(categoryId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Category deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    // Sub-subcategory endpoints
    @PostMapping("/sub-subcategories")
    public ResponseEntity<ApiResponse<SubSubcategory>> createSubSubcategory(
            @RequestBody SubSubcategoryCreateRequest request) {
        try {
            SubSubcategory subSubcategory = categoryService.createSubSubcategory(request.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.SUCCESS(HttpStatus.CREATED.toString(),
                            "Sub-subcategory created successfully", subSubcategory));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @GetMapping("/sub-subcategories")
    public ResponseEntity<ApiResponse<List<SubSubcategory>>> getAllSubSubcategory() {
        try {
            List<SubSubcategory> subSubcategories = categoryService.readAllSubSubcategories_v2();
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Sub-subcategory retrieved successfully", subSubcategories));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @GetMapping("/sub-subcategories/{subSubcategoryId}")
    public ResponseEntity<ApiResponse<SubSubcategory>> getSubSubcategory(
            @PathVariable Long subSubcategoryId) {
        try {
            SubSubcategory subSubcategory = categoryService.readSubSubcategory(subSubcategoryId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Sub-subcategory retrieved successfully", subSubcategory));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @GetMapping("/{subcategoryId}/sub-subcategories")
    public ResponseEntity<ApiResponse<List<SubSubcategory>>> getAllSubSubcategories(
            @PathVariable Long subcategoryId) {
        List<SubSubcategory> subSubcategories = categoryService.readAllSubSubcategories(subcategoryId);
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                        "Sub-subcategories retrieved successfully", subSubcategories));
    }
    
    @PutMapping("/sub-subcategories/{subSubcategoryId}")
    public ResponseEntity<ApiResponse<SubSubcategory>> updateSubSubcategory(
            @PathVariable Long subSubcategoryId,
            @RequestBody SubSubcategoryCreateRequest request) {
        try {
            SubSubcategory updated = categoryService.updateSubSubcategory(
                    subSubcategoryId, request.toEntity());
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Sub-subcategory updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/sub-subcategories/{subSubcategoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubSubcategory(
            @PathVariable Long subSubcategoryId) {
        try {
            categoryService.deleteSubSubcategory(subSubcategoryId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(),
                            "Sub-subcategory deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(), null));
        }
    }
}