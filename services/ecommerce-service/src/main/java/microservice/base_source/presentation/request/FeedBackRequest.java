package microservice.base_source.presentation.request;

import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.FeedBack;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedBackRequest {

	private Long replyId;

	private String buyerId;

	private Long productDetailId;

	private BigDecimal rating;

	private String content;

	private String img;

    private Map<String, Object> detail;

    public FeedBack toEntity() {
        FeedBack f = new FeedBack();
        f.setReplyId(this.replyId);
        f.setBuyerId(this.buyerId);
        f.setProductDetailId(this.productDetailId);
        f.setRating(this.rating);
        f.setContent(this.content);
        f.setImg(this.img);
        f.setDetail(this.detail);
        return f;
    }
}
