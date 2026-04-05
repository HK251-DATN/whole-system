package edu.hcmut.datn.back_office_service.repository;

import edu.hcmut.datn.back_office_service.dao.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find all main categories (isSubCategory = "N" and belongToCategory is null)
    @Query("SELECT c FROM Category c WHERE c.isSubCategory = 'N' AND c.belongToCategory IS NULL ORDER BY c.displayOrder")
    List<Category> findAllMainCategories();
    
    // Find subcategories by parent category ID
    @Query("SELECT c FROM Category c WHERE c.isSubCategory = 'Y' AND c.belongToCategory = :parentId ORDER BY c.displayOrder")
    List<Category> findSubcategoriesByParentId(@Param("parentId") Long parentId);
    
    // Check if category exists and is a subcategory
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.categoryId = :categoryId AND c.isSubCategory = 'Y'")
    boolean existsAsSubcategory(@Param("categoryId") Long categoryId);
}
