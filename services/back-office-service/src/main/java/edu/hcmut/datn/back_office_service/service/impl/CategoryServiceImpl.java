package edu.hcmut.datn.back_office_service.service.impl;

import edu.hcmut.datn.back_office_service.dao.Category;
import edu.hcmut.datn.back_office_service.dao.SubSubcategory;
import edu.hcmut.datn.back_office_service.messaging.category.CategoryCreatedEvent;
import edu.hcmut.datn.back_office_service.messaging.category.CategoryProducer;
import edu.hcmut.datn.back_office_service.messaging.category.SubSubcategoryCreatedEvent;
import edu.hcmut.datn.back_office_service.repository.CategoryRepository;
import edu.hcmut.datn.back_office_service.repository.SubSubcategoryRepository;
import edu.hcmut.datn.back_office_service.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final SubSubcategoryRepository subSubcategoryRepository;
    private final CategoryProducer categoryProducer;
    
    @Override
    @Transactional
    public Category create(Category category) {
        // Validate parent category if this is a subcategory
        if ("Y".equals(category.getIsSubCategory()) && category.getBelongToCategory() != null) {
            categoryRepository.findById(category.getBelongToCategory())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        // Publish event
        CategoryCreatedEvent event = new CategoryCreatedEvent(
                savedCategory.getCategoryId(),
                savedCategory.getName(),
                savedCategory.getDescription(),
                savedCategory.getDisplayOrder(),
                savedCategory.getIconUrl(),
                savedCategory.getIsSubCategory(),
                savedCategory.getBelongToCategory()
        );
        
        categoryProducer.publishCategoryCreated(event);
        log.info("Published CategoryCreatedEvent for category: {}", savedCategory.getCategoryId());
        
        return savedCategory;
    }
    
    @Override
    public Category read(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
    }
    
    @Override
    public List<Category> readAll() {
        return categoryRepository.findAllMainCategories();
    }
    
    @Override
    public List<Category> readSubcategories(Long parentCategoryId) {
        return categoryRepository.findSubcategoriesByParentId(parentCategoryId);
    }
    
    @Override
    @Transactional
    public Category update(Long categoryId, Category category) {
        Category existingCategory = read(categoryId);
        
        if (category.getName() != null) {
            existingCategory.setName(category.getName());
        }
        if (category.getDescription() != null) {
            existingCategory.setDescription(category.getDescription());
        }
        if (category.getDisplayOrder() != null) {
            existingCategory.setDisplayOrder(category.getDisplayOrder());
        }
        if (category.getIconUrl() != null) {
            existingCategory.setIconUrl(category.getIconUrl());
        }
        
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = read(categoryId);
        categoryRepository.delete(category);
    }
    
    // Sub-subcategory operations
    @Override
    @Transactional
    public SubSubcategory createSubSubcategory(SubSubcategory subSubcategory) {
        // Validate that parent is a subcategory
        if (!categoryRepository.existsAsSubcategory(subSubcategory.getSubcategoryId())) {
            throw new RuntimeException("Parent must be a subcategory (isSubCategory = Y)");
        }
        
        SubSubcategory saved = subSubcategoryRepository.save(subSubcategory);
        
        // Publish event
        SubSubcategoryCreatedEvent event = new SubSubcategoryCreatedEvent(
                saved.getSubSubcategoryId(),
                saved.getName(),
                saved.getDescription(),
                saved.getIconUrl(),
                saved.getSubcategoryId()
        );
        
        categoryProducer.publishSubSubcategoryCreated(event);
        log.info("Published SubSubcategoryCreatedEvent for sub-subcategory: {}", saved.getSubSubcategoryId());
        
        return saved;
    }
    
    @Override
    public SubSubcategory readSubSubcategory(Long subSubcategoryId) {
        return subSubcategoryRepository.findById(subSubcategoryId)
                .orElseThrow(() -> new RuntimeException("Sub-subcategory not found: " + subSubcategoryId));
    }
    
    @Override
    public List<SubSubcategory> readAllSubSubcategories_v2() {
        return subSubcategoryRepository.findAll();
    }
    
    @Override
    public List<SubSubcategory> readAllSubSubcategories(Long subcategoryId) {
        return subSubcategoryRepository.findBySubcategoryId(subcategoryId);
    }
    
    @Override
    @Transactional
    public SubSubcategory updateSubSubcategory(Long subSubcategoryId, SubSubcategory subSubcategory) {
        SubSubcategory existing = readSubSubcategory(subSubcategoryId);
        
        if (subSubcategory.getName() != null) {
            existing.setName(subSubcategory.getName());
        }
        if (subSubcategory.getDescription() != null) {
            existing.setDescription(subSubcategory.getDescription());
        }
        if (subSubcategory.getIconUrl() != null) {
            existing.setIconUrl(subSubcategory.getIconUrl());
        }
        
        return subSubcategoryRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteSubSubcategory(Long subSubcategoryId) {
        SubSubcategory subSubcategory = readSubSubcategory(subSubcategoryId);
        subSubcategoryRepository.delete(subSubcategory);
    }
}
