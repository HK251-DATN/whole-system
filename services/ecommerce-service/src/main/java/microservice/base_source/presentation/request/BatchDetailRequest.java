package microservice.base_source.presentation.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.BatchDetail;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailRequest {
    @NotNull
    private String batchDetailId;

    @NotNull
	private Long productGeneralId;

    @NotNull
	private int quantity;

    @NotNull
	private BigDecimal price;

	private BigDecimal avgRate;

	private int numRate;

    private String detailContent;
    public BatchDetail toEntity() {
        BatchDetail b = new BatchDetail();
        b.setBatchDetailId(this.batchDetailId);
        b.setProductGeneralId(this.productGeneralId);
        b.setQuantity(this.quantity);
        b.setPrice(this.price);
        b.setAvgRate(this.avgRate);
        b.setNumRate(this.numRate);
        b.setDetailContent(this.detailContent);
        return b;
    }
}
