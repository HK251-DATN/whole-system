package edu.hcmut.datn.back_office_service.repository;

import edu.hcmut.datn.back_office_service.dao.SubSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubSubcategoryRepository extends JpaRepository<SubSubcategory, Long> {
    
    // Find all sub-subcategories by subcategory ID
    List<SubSubcategory> findBySubcategoryId(Long subcategoryId);
    
    // Find all sub-subcategories with category hierarchy
    @Query("""
        SELECT ssc FROM SubSubcategory ssc
        WHERE ssc.subcategoryId IN (
            SELECT c.categoryId FROM Category c WHERE c.belongToCategory = :mainCategoryId
        )
    """)
    List<SubSubcategory> findByMainCategoryId(@Param("mainCategoryId") Long mainCategoryId);
}