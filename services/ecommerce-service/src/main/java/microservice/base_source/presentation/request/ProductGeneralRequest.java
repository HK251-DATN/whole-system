package microservice.base_source.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.ProductGeneral;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductGeneralRequest {
	@NotBlank
    @Size(max = 250)
    private String name;

    @NotNull
    Long categoryId;

    private String description;

    private String status;

    private String[] tags;

    private String img;

    public ProductGeneral toEntity() {
        ProductGeneral p = new ProductGeneral();
        p.setName(this.name);
        p.setCategoryId(this.categoryId);
        p.setDescription(this.description);
        p.setStatus(this.status);
        p.setImg(this.img);
        p.setTags(this.tags);
        return p;
    }
}
