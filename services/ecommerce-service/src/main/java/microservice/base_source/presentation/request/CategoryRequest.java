package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
	@NotBlank
    private String categoryName;

    private Integer displayOrder;

    private String description;

    private String iconUrl;

    private String isSubCategory;

    private Long belongToCategory;

    public Category toEntity() {
        Category c = new Category();
        c.setCategoryName(this.categoryName);
        c.setDisplayOrder(this.displayOrder);
        c.setDescription(this.description);
        c.setIconUrl(this.iconUrl);
        c.setIsSubCategory(this.isSubCategory);
        c.setBelongToCategory(this.belongToCategory);
        return c;
    }
}
