package com.smartwater.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usage_id")
    private WaterUsage waterUsage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    private BillingCycle billingCycle;

    @Column(name = "bill_month", nullable = false)
    private Integer billMonth;

    @Column(name = "bill_year", nullable = false)
    private Integer billYear;

    @Column(nullable = false)
    private Double units;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "tier1_units", precision = 10, scale = 2)
    private BigDecimal tier1Units;

    @Column(name = "tier1_rate", precision = 10, scale = 2)
    private BigDecimal tier1Rate;

    @Column(name = "tier1_amount", precision = 10, scale = 2)
    private BigDecimal tier1Amount;

    @Column(name = "tier2_units", precision = 10, scale = 2)
    private BigDecimal tier2Units;

    @Column(name = "tier2_rate", precision = 10, scale = 2)
    private BigDecimal tier2Rate;

    @Column(name = "tier2_amount", precision = 10, scale = 2)
    private BigDecimal tier2Amount;

    @Column(name = "tier3_units", precision = 10, scale = 2)
    private BigDecimal tier3Units;

    @Column(name = "tier3_rate", precision = 10, scale = 2)
    private BigDecimal tier3Rate;

    @Column(name = "tier3_amount", precision = 10, scale = 2)
    private BigDecimal tier3Amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BillStatus status;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    public Bill() {}

    public Bill(Long billId, Resident resident, WaterUsage waterUsage, BillingCycle billingCycle, Integer billMonth, Integer billYear, Double units, Double amount, BigDecimal tier1Units, BigDecimal tier1Rate, BigDecimal tier1Amount, BigDecimal tier2Units, BigDecimal tier2Rate, BigDecimal tier2Amount, BigDecimal tier3Units, BigDecimal tier3Rate, BigDecimal tier3Amount, LocalDate dueDate, BillStatus status, LocalDateTime generatedAt) {
        this.billId = billId;
        this.resident = resident;
        this.waterUsage = waterUsage;
        this.billingCycle = billingCycle;
        this.billMonth = billMonth;
        this.billYear = billYear;
        this.units = units;
        this.amount = amount;
        this.tier1Units = tier1Units;
        this.tier1Rate = tier1Rate;
        this.tier1Amount = tier1Amount;
        this.tier2Units = tier2Units;
        this.tier2Rate = tier2Rate;
        this.tier2Amount = tier2Amount;
        this.tier3Units = tier3Units;
        this.tier3Rate = tier3Rate;
        this.tier3Amount = tier3Amount;
        this.dueDate = dueDate;
        this.status = status;
        this.generatedAt = generatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BillStatus.UNPAID;
        }
    }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Resident getResident() { return resident; }
    public void setResident(Resident resident) { this.resident = resident; }

    public WaterUsage getWaterUsage() { return waterUsage; }
    public void setWaterUsage(WaterUsage waterUsage) { this.waterUsage = waterUsage; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public Integer getBillMonth() { return billMonth; }
    public void setBillMonth(Integer billMonth) { this.billMonth = billMonth; }

    public Integer getBillYear() { return billYear; }
    public void setBillYear(Integer billYear) { this.billYear = billYear; }

    public Double getUnits() { return units; }
    public void setUnits(Double units) { this.units = units; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public BigDecimal getTier1Units() { return tier1Units; }
    public void setTier1Units(BigDecimal tier1Units) { this.tier1Units = tier1Units; }

    public BigDecimal getTier1Rate() { return tier1Rate; }
    public void setTier1Rate(BigDecimal tier1Rate) { this.tier1Rate = tier1Rate; }

    public BigDecimal getTier1Amount() { return tier1Amount; }
    public void setTier1Amount(BigDecimal tier1Amount) { this.tier1Amount = tier1Amount; }

    public BigDecimal getTier2Units() { return tier2Units; }
    public void setTier2Units(BigDecimal tier2Units) { this.tier2Units = tier2Units; }

    public BigDecimal getTier2Rate() { return tier2Rate; }
    public void setTier2Rate(BigDecimal tier2Rate) { this.tier2Rate = tier2Rate; }

    public BigDecimal getTier2Amount() { return tier2Amount; }
    public void setTier2Amount(BigDecimal tier2Amount) { this.tier2Amount = tier2Amount; }

    public BigDecimal getTier3Units() { return tier3Units; }
    public void setTier3Units(BigDecimal tier3Units) { this.tier3Units = tier3Units; }

    public BigDecimal getTier3Rate() { return tier3Rate; }
    public void setTier3Rate(BigDecimal tier3Rate) { this.tier3Rate = tier3Rate; }

    public BigDecimal getTier3Amount() { return tier3Amount; }
    public void setTier3Amount(BigDecimal tier3Amount) { this.tier3Amount = tier3Amount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BillStatus getStatus() { return status; }
    public void setStatus(BillStatus status) { this.status = status; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long billId;
        private Resident resident;
        private WaterUsage waterUsage;
        private BillingCycle billingCycle;
        private Integer billMonth;
        private Integer billYear;
        private Double units;
        private Double amount;
        private BigDecimal tier1Units;
        private BigDecimal tier1Rate;
        private BigDecimal tier1Amount;
        private BigDecimal tier2Units;
        private BigDecimal tier2Rate;
        private BigDecimal tier2Amount;
        private BigDecimal tier3Units;
        private BigDecimal tier3Rate;
        private BigDecimal tier3Amount;
        private LocalDate dueDate;
        private BillStatus status;
        private LocalDateTime generatedAt;

        public Builder billId(Long billId) { this.billId = billId; return this; }
        public Builder resident(Resident resident) { this.resident = resident; return this; }
        public Builder waterUsage(WaterUsage waterUsage) { this.waterUsage = waterUsage; return this; }
        public Builder billingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; return this; }
        public Builder billMonth(Integer billMonth) { this.billMonth = billMonth; return this; }
        public Builder billYear(Integer billYear) { this.billYear = billYear; return this; }
        public Builder units(Double units) { this.units = units; return this; }
        public Builder amount(Double amount) { this.amount = amount; return this; }
        public Builder tier1Units(BigDecimal tier1Units) { this.tier1Units = tier1Units; return this; }
        public Builder tier1Rate(BigDecimal tier1Rate) { this.tier1Rate = tier1Rate; return this; }
        public Builder tier1Amount(BigDecimal tier1Amount) { this.tier1Amount = tier1Amount; return this; }
        public Builder tier2Units(BigDecimal tier2Units) { this.tier2Units = tier2Units; return this; }
        public Builder tier2Rate(BigDecimal tier2Rate) { this.tier2Rate = tier2Rate; return this; }
        public Builder tier2Amount(BigDecimal tier2Amount) { this.tier2Amount = tier2Amount; return this; }
        public Builder tier3Units(BigDecimal tier3Units) { this.tier3Units = tier3Units; return this; }
        public Builder tier3Rate(BigDecimal tier3Rate) { this.tier3Rate = tier3Rate; return this; }
        public Builder tier3Amount(BigDecimal tier3Amount) { this.tier3Amount = tier3Amount; return this; }
        public Builder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder status(BillStatus status) { this.status = status; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }

        public Bill build() {
            return new Bill(billId, resident, waterUsage, billingCycle, billMonth, billYear, units, amount, tier1Units, tier1Rate, tier1Amount, tier2Units, tier2Rate, tier2Amount, tier3Units, tier3Rate, tier3Amount, dueDate, status, generatedAt);
        }
    }

    public enum BillStatus { UNPAID, PAID, OVERDUE }
}
