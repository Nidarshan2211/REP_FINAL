package com.smartwater.billing.owner.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tariff")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tariff_id")
    private Long tariffId;

    @Column(name = "minimum_units", nullable = false)
    private Integer minimumUnits;

    @Column(name = "maximum_units", nullable = false)
    private Integer maximumUnits;

    @Column(name = "price_per_unit", nullable = false)
    private Double pricePerUnit;

    @Column(name = "fixed_charge", nullable = false)
    private Double fixedCharge;

    public Tariff() {}

    public Tariff(Long tariffId, Integer minimumUnits, Integer maximumUnits, Double pricePerUnit, Double fixedCharge) {
        this.tariffId = tariffId;
        this.minimumUnits = minimumUnits;
        this.maximumUnits = maximumUnits;
        this.pricePerUnit = pricePerUnit;
        this.fixedCharge = fixedCharge;
    }

    public Long getTariffId() { return tariffId; }
    public void setTariffId(Long tariffId) { this.tariffId = tariffId; }

    public Integer getMinimumUnits() { return minimumUnits; }
    public void setMinimumUnits(Integer minimumUnits) { this.minimumUnits = minimumUnits; }

    public Integer getMaximumUnits() { return maximumUnits; }
    public void setMaximumUnits(Integer maximumUnits) { this.maximumUnits = maximumUnits; }

    public Double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(Double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public Double getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(Double fixedCharge) { this.fixedCharge = fixedCharge; }
}
