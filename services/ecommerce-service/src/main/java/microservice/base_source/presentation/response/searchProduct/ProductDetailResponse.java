package microservice.base_source.presentation.response.searchProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductDetailResponse {
	private Long productDetailId;
    private String description;
    private String status;
    private Integer quantityAvailable;
    private BigDecimal price;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    // private LocalDateTime updatedAt;
}
