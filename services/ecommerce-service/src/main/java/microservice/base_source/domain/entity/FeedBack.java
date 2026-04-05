package microservice.base_source.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FEED_BACK")
public class FeedBack {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "feedback_id")
	private Long feedbackId;

	@Column(name = "reply_id", nullable = true)
	private Long replyId;

	@Column(name = "buyer_id")
	private String buyerId;

	@Column(name = "product_detail_id")
	private Long productDetailId;

	@Column(name = "rating", precision = 3, scale = 2)
	private BigDecimal rating;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "img", columnDefinition = "TEXT")
	private String img;

	@JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detail")
    private Map<String, Object> detail; // HTML, CSS, FILE, VIDEO

	@Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delete_at")
    private LocalDateTime deletedAt;

	@PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PreRemove
    public void preRemove() {
        deletedAt = LocalDateTime.now();
    }
}
