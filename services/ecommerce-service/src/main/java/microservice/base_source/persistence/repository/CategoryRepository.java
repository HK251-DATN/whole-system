package microservice.base_source.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.Category;
import microservice.base_source.persistence.dto.CategoryDTO;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query(
		value = """
            SELECT *
            FROM CATEGORY c
            WHERE c.is_sub_category = 'Y'
				AND (
					:searchString = '' OR
					LOWER(c.category_name) LIKE LOWER(CONCAT('%', :searchString, '%'))
					OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchString, '%'))
				)
            LIMIT :size 
			OFFSET ((:page - 1) * :size)
            """,
		nativeQuery = true
	)
	List<Category> search(
		@Param("searchString") String searchString, 
		@Param("page") Integer page, 
		@Param("size") Integer size
	);

	@Query(
		value = """
            WITH LIST_CATEGORY AS (
				SELECT *
				FROM CATEGORY
				WHERE IS_SUB_CATEGORY = 'N'
			)
			SELECT
				LS.CATEGORY_ID   AS parentId,
				LS.CATEGORY_NAME AS parentName,
				LS.DESCRIPTION 	 AS parentDescription,
				LS.ICON_URL 	 AS parenticonUrl,
				LS.DISPLAY_ORDER AS parentDisplayOrder,
				C.CATEGORY_ID    AS subId,
				C.CATEGORY_NAME  AS subName,
				C.DESCRIPTION 	 AS subparentDescription,
				C.ICON_URL 		 AS subiconUrl,
				C.DISPLAY_ORDER  AS subDisplayOrder
			FROM LIST_CATEGORY LS
			JOIN CATEGORY C
				ON LS.CATEGORY_ID = C.BELONG_TO_CATEGORY;
            """,
		nativeQuery = true
	)
	List<CategoryDTO> getAll();
}
