package com.smartwater.billing.resident.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.smartwater.billing.owner.entity.Apartment;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "away_periods", indexes = {
    @Index(name = "idx_away_occupancy", columnList = "occupancy_id"),
    @Index(name = "idx_away_apartment", columnList = "apartment_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AwayPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupancy_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ApartmentOccupancy occupancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Apartment apartment;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AwayPeriod() {}

    public AwayPeriod(Long id, ApartmentOccupancy occupancy, Apartment apartment, LocalDate startDate, LocalDate endDate, String note, LocalDateTime createdAt) {
        this.id = id;
        this.occupancy = occupancy;
        this.apartment = apartment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.note = note;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ApartmentOccupancy getOccupancy() { return occupancy; }
    public void setOccupancy(ApartmentOccupancy occupancy) { this.occupancy = occupancy; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private ApartmentOccupancy occupancy;
        private Apartment apartment;
        private LocalDate startDate;
        private LocalDate endDate;
        private String note;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder occupancy(ApartmentOccupancy occupancy) { this.occupancy = occupancy; return this; }
        public Builder apartment(Apartment apartment) { this.apartment = apartment; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder note(String note) { this.note = note; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AwayPeriod build() {
            return new AwayPeriod(id, occupancy, apartment, startDate, endDate, note, createdAt);
        }
    }
}
