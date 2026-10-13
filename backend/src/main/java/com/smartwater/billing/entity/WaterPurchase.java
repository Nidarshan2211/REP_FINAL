package com.smartwater.billing.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_purchases")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WaterPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner"})
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "building"})
    private BillingCycle billingCycle;

    @Column(nullable = false, length = 100)
    private String supplier;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volume;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public WaterPurchase() {}

    public WaterPurchase(Long id, Building building, BillingCycle billingCycle, String supplier, BigDecimal volume, BigDecimal quantity, BigDecimal unitCost, BigDecimal totalCost, LocalDate purchaseDate, LocalDateTime createdAt) {
        this.id = id;
        this.building = building;
        this.billingCycle = billingCycle;
        this.supplier = supplier;
        this.volume = volume != null ? volume : quantity;
        this.quantity = quantity != null ? quantity : volume;
        this.unitCost = unitCost;
        this.totalCost = totalCost;
        this.purchaseDate = purchaseDate;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (purchaseDate == null) purchaseDate = LocalDate.now();
        if (quantity == null) quantity = volume;
        if (volume == null) volume = quantity;
        if (totalCost == null && quantity != null && unitCost != null) {
            totalCost = quantity.multiply(unitCost);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public BigDecimal getVolume() { return volume != null ? volume : quantity; }
    public void setVolume(BigDecimal volume) {
        this.volume = volume;
        this.quantity = volume;
    }

    public BigDecimal getQuantity() { return quantity != null ? quantity : volume; }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        this.volume = quantity;
    }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Building building;
        private BillingCycle billingCycle;
        private String supplier;
        private BigDecimal volume;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
        private LocalDate purchaseDate;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder building(Building building) { this.building = building; return this; }
        public Builder billingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; return this; }
        public Builder supplier(String supplier) { this.supplier = supplier; return this; }
        public Builder volume(BigDecimal volume) {
            this.volume = volume;
            this.quantity = volume;
            return this;
        }
        public Builder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            this.volume = quantity;
            return this;
        }
        public Builder unitCost(BigDecimal unitCost) { this.unitCost = unitCost; return this; }
        public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
        public Builder purchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public WaterPurchase build() {
            return new WaterPurchase(id, building, billingCycle, supplier, volume, quantity, unitCost, totalCost, purchaseDate, createdAt);
        }
    }
}
