package microservice.base_source.presentation.response.searchProduct;

import lombok.Data;

@Data
public class ProductGeneralResponse {
	private Long productGeneralId;
    private Long categoryId;
    private String productName;
    private String description;
    private String status;
    private String photoUrls;
}
