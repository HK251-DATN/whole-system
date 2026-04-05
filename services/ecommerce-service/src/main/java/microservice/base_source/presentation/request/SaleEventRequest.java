package microservice.base_source.presentation.request;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import microservice.base_source.domain.entity.SaleEvent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleEventRequest {
	@NotBlank
    private String name;

    private String description;

    private String img;

    private Long displayPriority;

    private String activeYn;

    private String enabledYn;

	private LocalTime beginTime;

	private LocalTime endTime;

	private LocalDateTime beginDate;

	private LocalDateTime endDate;

	private Object detail; 

    public SaleEvent toEntity() {
        SaleEvent s = new SaleEvent();
        s.setName(this.name);
        s.setDescription(this.description);
        s.setImg(this.img);
        s.setDisplayPriority(this.displayPriority);
        s.setActiveYn(this.activeYn);
        s.setEnabledYn(this.enabledYn);
        s.setBeginTime(this.beginTime);
        s.setEndTime(this.endTime);
        s.setBeginDate(this.beginDate);
        s.setEndDate(this.endDate);
        return s;
    }
}
