package edu.hcmut.datn.back_office_service.dto.request.event;

import java.time.LocalDate;
import java.time.LocalTime;

import edu.hcmut.datn.back_office_service.dao.SaleEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SaleEventCreateRequest extends EventCreateRequest {

    private String name;
    private String description;
    private String img;
    private Long displayPriority;
    private Boolean saleEventIsActive;
    private LocalDate saleEventBeginDate;
    private LocalDate saleEventEndDate;
    private LocalTime saleEventBeginTime;
    private LocalTime saleEventEndTime;

    public SaleEvent toSaleEventEntity() {
        return new SaleEvent(name, description, img, displayPriority, saleEventIsActive, saleEventBeginDate, saleEventEndDate,
                saleEventBeginTime, saleEventEndTime);
    }

    @Override
    public String toString() {

        return super.toString() + "SaleEventCreateRequest [name=" + name + ", description=" + description + ", img=" + img
                + ", displayPriority=" + displayPriority + ", saleEventIsActive=" + saleEventIsActive
                + ", saleEventBeginDate=" + saleEventBeginDate + ", saleEventEndDate=" + saleEventEndDate
                + ", saleEventBeginTime=" + saleEventBeginTime + ", saleEventEndTime=" + saleEventEndTime + "]";
    }
}
