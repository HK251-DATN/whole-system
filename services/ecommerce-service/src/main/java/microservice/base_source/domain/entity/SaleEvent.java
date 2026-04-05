package microservice.base_source.domain.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
@Table(name = "SALE_EVENT")
public class SaleEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sale_event_id")
    private Long saleEventId;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "img", columnDefinition = "TEXT")
	private String img;

	@Column(name = "display_priority")
	private Long displayPriority;

	@Column(name = "active_yn")
	private String activeYn;

	@Column(name = "enabled_yn")
	private String enabledYn;

	@Column(name = "begin_time")
	private LocalTime beginTime;

	@Column(name = "end_time")
	private LocalTime endTime;

	@Column(name = "begin_date")
	private LocalDateTime beginDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "detail")
	private Object detail; // JSON format

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "delete_at")
    private LocalDateTime deletedAt;

	public enum LoopOption {
		DAY,
		WEEK,
		MONTH
	}

	public enum SaleEventStatus {
		ACTIVE,
		INACTIVE,
		DELETED,
		COMPLETED
	}

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
