package com.smartwater.billing.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "water_meters")
public class WaterMeter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meter_id")
    private Long meterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @Column(name = "meter_number", nullable = false, unique = true, length = 50)
    private String meterNumber;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeterStatus status;

    public WaterMeter() {}

    public WaterMeter(Long meterId, Building building, Resident resident, String meterNumber, LocalDate installationDate, MeterStatus status) {
        this.meterId = meterId;
        this.building = building;
        this.resident = resident;
        this.meterNumber = meterNumber;
        this.installationDate = installationDate;
        this.status = status;
    }

    public Long getMeterId() { return meterId; }
    public void setMeterId(Long meterId) { this.meterId = meterId; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public MeterStatus getStatus() { return status; }
    public void setStatus(MeterStatus status) { this.status = status; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long meterId;
        private Building building;
        private Resident resident;
        private String meterNumber;
        private LocalDate installationDate;
        private MeterStatus status;

        public Builder meterId(Long meterId) { this.meterId = meterId; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder resident(Resident resident) { this.resident = resident; return this; }
        public Builder meterNumber(String meterNumber) { this.meterNumber = meterNumber; return this; }
        public Builder installationDate(LocalDate installationDate) { this.installationDate = installationDate; return this; }
        public Builder status(MeterStatus status) { this.status = status; return this; }

        public WaterMeter build() {
            return new WaterMeter(meterId, building, resident, meterNumber, installationDate, status);
        }
    }

    public enum MeterStatus { ACTIVE, INACTIVE, FAULTY }
}
