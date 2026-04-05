package microservice.base_source.presentation.response.searchProduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import microservice.base_source.persistence.dto.DetailGeneralDTO;

@Getter
@Setter
public class ProductSearchResponse {
	private Long   	      productGeneralId;
    private Long   	      categoryId;
    private String 	      providerId;
    private String 	      name;
    private String 	      description;
    private String 	      img;
    private String 	      batchId;
    private Integer       quantity;
    private BigDecimal 	  originPrice;
    private BigDecimal 	  salePrice;
    private BigDecimal 	  disVal;
    private BigDecimal 	  avgRate; 
    private int 	      numRate;
    private Long          saleEventId;
    private LocalDateTime createdAt;

    public static ProductSearchResponse toResponse(DetailGeneralDTO dto) {
        ProductSearchResponse response = new ProductSearchResponse();
        response.setProductGeneralId(dto.getProductGeneralId());
        response.setCategoryId(dto.getCategoryId());
        response.setProviderId(dto.getProviderId());
        response.setName(dto.getName());
        response.setDescription(dto.getDescription());
        response.setImg(dto.getImg());
        response.setBatchId(dto.getBatchId());
        response.setQuantity(dto.getQuantity());
        response.setOriginPrice(dto.getOriginPrice());
        response.setDisVal(dto.getDisVal());
        response.setSalePrice(calculateSalePrice(dto.getOriginPrice(), dto.getDisVal()));
        response.setAvgRate(dto.getAvgRate());
        response.setNumRate(dto.getNumRate());
        response.setSaleEventId(dto.getSaleEventId());
        response.setCreatedAt(dto.getCreatedAt());
        return response;
    }

    public static BigDecimal calculateSalePrice(BigDecimal originPrice, BigDecimal disVal) {
        if (disVal == null || disVal.compareTo(BigDecimal.ZERO) == 0) {
            return originPrice;
        }
        else {
            return originPrice.multiply(disVal).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }
}
