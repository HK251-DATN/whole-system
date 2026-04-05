package edu.hcmut.datn.back_office_service.service;

import edu.hcmut.datn.back_office_service.dao.Category;
import edu.hcmut.datn.back_office_service.dao.SubSubcategory;

import java.util.List;

public interface CategoryService {
    Category create(Category category);
    Category read(Long categoryId);
    List<Category> readAll();
    List<Category> readSubcategories(Long parentCategoryId);
    Category update(Long categoryId, Category category);
    void delete(Long categoryId);
    
    // Sub-subcategory operations
    SubSubcategory createSubSubcategory(SubSubcategory subSubcategory);
    SubSubcategory readSubSubcategory(Long subSubcategoryId);
    List<SubSubcategory> readAllSubSubcategories_v2();
    List<SubSubcategory> readAllSubSubcategories(Long subcategoryId);
    SubSubcategory updateSubSubcategory(Long subSubcategoryId, SubSubcategory subSubcategory);
    void deleteSubSubcategory(Long subSubcategoryId);
}
