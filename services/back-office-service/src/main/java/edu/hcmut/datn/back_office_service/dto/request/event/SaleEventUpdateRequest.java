package edu.hcmut.datn.back_office_service.dto.request.event;

import java.time.LocalDate;
import java.time.LocalTime;

import edu.hcmut.datn.back_office_service.dao.SaleEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SaleEventUpdateRequest {
    private String name;
    private String description;
    private String img;
    private Long displayPriority;
    private Boolean isActive;
    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime beginTime;
    private LocalTime endTime;

    public SaleEvent toEntity() {
        return new SaleEvent(name, description, img, displayPriority, isActive, beginDate, endDate, beginTime, endTime);
    }
}
