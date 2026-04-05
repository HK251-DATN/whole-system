package microservice.base_source.presentation.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.SaleProduct;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleProductRequest {
    @NotNull
	private Long saleEventId;

	private String batchId;

	private BigDecimal disVal;

	private Long maxQty;

	private Long curQty;

	private Long maxBuy;

    public SaleProduct toEntity() {
        SaleProduct sp = new SaleProduct();
        sp.setSaleEventId(this.saleEventId);
        sp.setBatchId(this.batchId);
        sp.setDisVal(this.disVal);
        sp.setMaxQty(this.maxQty);
        sp.setCurQty(this.curQty);
        sp.setMaxBuy(this.maxBuy);
        return sp;
    }
}
