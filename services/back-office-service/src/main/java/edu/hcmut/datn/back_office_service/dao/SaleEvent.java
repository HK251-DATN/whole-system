package edu.hcmut.datn.back_office_service.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "sale_events")
@NoArgsConstructor
@ToString
public class SaleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_event_id")
    @Getter
    private String saleEventId;

    @Getter
    @Setter
    @Column(name = "name")
    private String name;

    @Getter
    @Setter
    @Column(name = "description")
    private String description;

    @Getter
    @Setter
    @Column(name = "img")
    private String img;

    @Getter
    @Setter
    @Column(name = "display_priority")
    private Long displayPriority;

    @Getter
    @Setter
    @Column(name = "is_active")
    private Boolean isActive;

    @Getter
    @Setter
    @Column(name = "begin_date")
    private LocalDate beginDate;

    @Getter
    @Setter
    @Column(name = "end_date")
    private LocalDate endDate;

    @Getter
    @Setter
    @Column(name = "begin_time")
    private LocalTime beginTime;

    @Getter
    @Setter
    @Column(name = "end_time")
    private LocalTime endTime;

    @Getter
    @Setter
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SaleEvent(String name, String description, String img, Long displayPriority, Boolean isActive,
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime, Long eventId) {

        this.name = name;
        this.description = description;
        this.img = img;
        this.displayPriority = displayPriority;
        this.isActive = isActive;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.eventId = eventId;
    }

    public SaleEvent(String name, String description, String img, Long displayPriority, Boolean isActive,
            LocalDate beginDate, LocalDate endDate, LocalTime beginTime, LocalTime endTime) {
        this.name = name;
        this.description = description;
        this.img = img;
        this.displayPriority = displayPriority;
        this.isActive = isActive;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Set createdAt on first save
        updatedAt = LocalDateTime.now(); // Optional: Set initial updatedAt
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update on every save after creation
    }

}
