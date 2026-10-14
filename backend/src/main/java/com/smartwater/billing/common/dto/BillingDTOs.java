package com.smartwater.billing.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BillingDTOs {

    public static class GenerateBillRequest {
        @NotNull(message = "Resident selection is required")
        private Long residentId;

        @NotNull(message = "Usage selection is required")
        private Long usageId;

        @NotNull(message = "Due date is required")
        private LocalDate dueDate;

        public GenerateBillRequest() {}
        public GenerateBillRequest(Long residentId, Long usageId, LocalDate dueDate) {
            this.residentId = residentId;
            this.usageId = usageId;
            this.dueDate = dueDate;
        }

        public Long getResidentId() { return residentId; }
        public void setResidentId(Long residentId) { this.residentId = residentId; }

        public Long getUsageId() { return usageId; }
        public void setUsageId(Long usageId) { this.usageId = usageId; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    }

    public static class BillResponse {
        private Long billId;
        private Long residentId;
        private String residentName;
        private String residentEmail;
        private String apartmentNumber;
        private String buildingName;
        private Integer billMonth;
        private Integer billYear;
        private Double units;
        private Double amount;
        private Double baseCharge;
        private Double extraCharge;
        private Double sharedWaterCharge;
        private LocalDate dueDate;
        private String status;
        private LocalDateTime generatedAt;

        private Long buildingId;
        private String buildingNumber;
        private Long billingCycleId;
        private Double tier1Units;
        private Double tier1Rate;
        private Double tier1Amount;
        private Double tier2Units;
        private Double tier2Rate;
        private Double tier2Amount;
        private Double tier3Units;
        private Double tier3Rate;
        private Double tier3Amount;

        public BillResponse() {}
        public BillResponse(Long billId, Long residentId, String residentName, String residentEmail, String apartmentNumber, String buildingName, Integer billMonth, Integer billYear, Double units, Double amount, Double baseCharge, Double extraCharge, Double sharedWaterCharge, LocalDate dueDate, String status, LocalDateTime generatedAt) {
            this.billId = billId;
            this.residentId = residentId;
            this.residentName = residentName;
            this.residentEmail = residentEmail;
            this.apartmentNumber = apartmentNumber;
            this.buildingName = buildingName;
            this.billMonth = billMonth;
            this.billYear = billYear;
            this.units = units;
            this.amount = amount;
            this.baseCharge = baseCharge;
            this.extraCharge = extraCharge;
            this.sharedWaterCharge = sharedWaterCharge;
            this.dueDate = dueDate;
            this.status = status;
            this.generatedAt = generatedAt;
        }

        public Long getBillId() { return billId; }
        public void setBillId(Long billId) { this.billId = billId; }

        public Long getResidentId() { return residentId; }
        public void setResidentId(Long residentId) { this.residentId = residentId; }

        public String getResidentName() { return residentName; }
        public void setResidentName(String residentName) { this.residentName = residentName; }

        public String getResidentEmail() { return residentEmail; }
        public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }

        public String getApartmentNumber() { return apartmentNumber; }
        public void setApartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; }

        public String getBuildingName() { return buildingName; }
        public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

        public Long getBuildingId() { return buildingId; }
        public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

        public String getBuildingNumber() { return buildingNumber; }
        public void setBuildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; }

        public Long getBillingCycleId() { return billingCycleId; }
        public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

        public Integer getBillMonth() { return billMonth; }
        public void setBillMonth(Integer billMonth) { this.billMonth = billMonth; }

        public Integer getBillYear() { return billYear; }
        public void setBillYear(Integer billYear) { this.billYear = billYear; }

        public Double getUnits() { return units; }
        public void setUnits(Double units) { this.units = units; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public Double getBaseCharge() { return baseCharge; }
        public void setBaseCharge(Double baseCharge) { this.baseCharge = baseCharge; }

        public Double getExtraCharge() { return extraCharge; }
        public void setExtraCharge(Double extraCharge) { this.extraCharge = extraCharge; }

        public Double getSharedWaterCharge() { return sharedWaterCharge; }
        public void setSharedWaterCharge(Double sharedWaterCharge) { this.sharedWaterCharge = sharedWaterCharge; }

        public Double getTier1Units() { return tier1Units; }
        public void setTier1Units(Double tier1Units) { this.tier1Units = tier1Units; }

        public Double getTier1Rate() { return tier1Rate; }
        public void setTier1Rate(Double tier1Rate) { this.tier1Rate = tier1Rate; }

        public Double getTier1Amount() { return tier1Amount; }
        public void setTier1Amount(Double tier1Amount) { this.tier1Amount = tier1Amount; }

        public Double getTier2Units() { return tier2Units; }
        public void setTier2Units(Double tier2Units) { this.tier2Units = tier2Units; }

        public Double getTier2Rate() { return tier2Rate; }
        public void setTier2Rate(Double tier2Rate) { this.tier2Rate = tier2Rate; }

        public Double getTier2Amount() { return tier2Amount; }
        public void setTier2Amount(Double tier2Amount) { this.tier2Amount = tier2Amount; }

        public Double getTier3Units() { return tier3Units; }
        public void setTier3Units(Double tier3Units) { this.tier3Units = tier3Units; }

        public Double getTier3Rate() { return tier3Rate; }
        public void setTier3Rate(Double tier3Rate) { this.tier3Rate = tier3Rate; }

        public Double getTier3Amount() { return tier3Amount; }
        public void setTier3Amount(Double tier3Amount) { this.tier3Amount = tier3Amount; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long billId;
            private Long residentId;
            private String residentName;
            private String residentEmail;
            private String apartmentNumber;
            private String buildingName;
            private Long buildingId;
            private String buildingNumber;
            private Long billingCycleId;
            private Integer billMonth;
            private Integer billYear;
            private Double units;
            private Double amount;
            private Double baseCharge;
            private Double extraCharge;
            private Double sharedWaterCharge;
            private Double tier1Units;
            private Double tier1Rate;
            private Double tier1Amount;
            private Double tier2Units;
            private Double tier2Rate;
            private Double tier2Amount;
            private Double tier3Units;
            private Double tier3Rate;
            private Double tier3Amount;
            private LocalDate dueDate;
            private String status;
            private LocalDateTime generatedAt;

            public Builder billId(Long billId) { this.billId = billId; return this; }
            public Builder residentId(Long residentId) { this.residentId = residentId; return this; }
            public Builder residentName(String residentName) { this.residentName = residentName; return this; }
            public Builder residentEmail(String residentEmail) { this.residentEmail = residentEmail; return this; }
            public Builder apartmentNumber(String apartmentNumber) { this.apartmentNumber = apartmentNumber; return this; }
            public Builder buildingName(String buildingName) { this.buildingName = buildingName; return this; }
            public Builder buildingId(Long buildingId) { this.buildingId = buildingId; return this; }
            public Builder buildingNumber(String buildingNumber) { this.buildingNumber = buildingNumber; return this; }
            public Builder billingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; return this; }
            public Builder billMonth(Integer billMonth) { this.billMonth = billMonth; return this; }
            public Builder billYear(Integer billYear) { this.billYear = billYear; return this; }
            public Builder units(Double units) { this.units = units; return this; }
            public Builder amount(Double amount) { this.amount = amount; return this; }
            public Builder baseCharge(Double baseCharge) { this.baseCharge = baseCharge; return this; }
            public Builder extraCharge(Double extraCharge) { this.extraCharge = extraCharge; return this; }
            public Builder sharedWaterCharge(Double sharedWaterCharge) { this.sharedWaterCharge = sharedWaterCharge; return this; }
            public Builder tier1Units(Double tier1Units) { this.tier1Units = tier1Units; return this; }
            public Builder tier1Rate(Double tier1Rate) { this.tier1Rate = tier1Rate; return this; }
            public Builder tier1Amount(Double tier1Amount) { this.tier1Amount = tier1Amount; return this; }
            public Builder tier2Units(Double tier2Units) { this.tier2Units = tier2Units; return this; }
            public Builder tier2Rate(Double tier2Rate) { this.tier2Rate = tier2Rate; return this; }
            public Builder tier2Amount(Double tier2Amount) { this.tier2Amount = tier2Amount; return this; }
            public Builder tier3Units(Double tier3Units) { this.tier3Units = tier3Units; return this; }
            public Builder tier3Rate(Double tier3Rate) { this.tier3Rate = tier3Rate; return this; }
            public Builder tier3Amount(Double tier3Amount) { this.tier3Amount = tier3Amount; return this; }
            public Builder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
            public Builder status(String status) { this.status = status; return this; }
            public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }

            public BillResponse build() {
                BillResponse response = new BillResponse(billId, residentId, residentName, residentEmail, apartmentNumber, buildingName, billMonth, billYear, units, amount, baseCharge, extraCharge, sharedWaterCharge, dueDate, status, generatedAt);
                response.setBuildingId(buildingId);
                response.setBuildingNumber(buildingNumber);
                response.setBillingCycleId(billingCycleId);
                response.setTier1Units(tier1Units);
                response.setTier1Rate(tier1Rate);
                response.setTier1Amount(tier1Amount);
                response.setTier2Units(tier2Units);
                response.setTier2Rate(tier2Rate);
                response.setTier2Amount(tier2Amount);
                response.setTier3Units(tier3Units);
                response.setTier3Rate(tier3Rate);
                response.setTier3Amount(tier3Amount);
                return response;
            }
        }
    }

    public static class InvoiceDashboardSummary {
        private Double totalBilled;
        private Double totalPaid;
        private Double totalUnpaid;
        private Double averageBilled;
        private Integer totalInvoices;
        private Integer paidInvoices;
        private Integer unpaidInvoices;
        private List<BillResponse> invoices;

        public InvoiceDashboardSummary() {}
        public InvoiceDashboardSummary(Double totalBilled, Double totalPaid, Double totalUnpaid, Double averageBilled, Integer totalInvoices, Integer paidInvoices, Integer unpaidInvoices, List<BillResponse> invoices) {
            this.totalBilled = totalBilled;
            this.totalPaid = totalPaid;
            this.totalUnpaid = totalUnpaid;
            this.averageBilled = averageBilled;
            this.totalInvoices = totalInvoices;
            this.paidInvoices = paidInvoices;
            this.unpaidInvoices = unpaidInvoices;
            this.invoices = invoices;
        }

        public Double getTotalBilled() { return totalBilled; }
        public void setTotalBilled(Double totalBilled) { this.totalBilled = totalBilled; }

        public Double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }

        public Double getTotalUnpaid() { return totalUnpaid; }
        public void setTotalUnpaid(Double totalUnpaid) { this.totalUnpaid = totalUnpaid; }

        public Double getAverageBilled() { return averageBilled; }
        public void setAverageBilled(Double averageBilled) { this.averageBilled = averageBilled; }

        public Integer getTotalInvoices() { return totalInvoices; }
        public void setTotalInvoices(Integer totalInvoices) { this.totalInvoices = totalInvoices; }

        public Integer getPaidInvoices() { return paidInvoices; }
        public void setPaidInvoices(Integer paidInvoices) { this.paidInvoices = paidInvoices; }

        public Integer getUnpaidInvoices() { return unpaidInvoices; }
        public void setUnpaidInvoices(Integer unpaidInvoices) { this.unpaidInvoices = unpaidInvoices; }

        public List<BillResponse> getInvoices() { return invoices; }
        public void setInvoices(List<BillResponse> invoices) { this.invoices = invoices; }
    }

    public static class PaymentRequest {
        @NotNull(message = "Bill ID is required")
        private Long billId;

        @NotNull(message = "Payment amount is required")
        private Double amount;

        @NotBlank(message = "Payment method is required")
        private String paymentMethod;

        private String transactionReference;

        public PaymentRequest() {}

        public Long getBillId() { return billId; }
        public void setBillId(Long billId) { this.billId = billId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    }

    public static class PaymentResponse {
        private Long paymentId;
        private Long billId;
        private Double amount;
        private LocalDateTime paymentDate;
        private String paymentMethod;
        private String transactionReference;
        private String status;

        public PaymentResponse() {}
        public PaymentResponse(Long paymentId, Long billId, Double amount, LocalDateTime paymentDate, String paymentMethod, String transactionReference, String status) {
            this.paymentId = paymentId;
            this.billId = billId;
            this.amount = amount;
            this.paymentDate = paymentDate;
            this.paymentMethod = paymentMethod;
            this.transactionReference = transactionReference;
            this.status = status;
        }

        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

        public Long getBillId() { return billId; }
        public void setBillId(Long billId) { this.billId = billId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public LocalDateTime getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long paymentId;
            private Long billId;
            private Double amount;
            private LocalDateTime paymentDate;
            private String paymentMethod;
            private String transactionReference;
            private String status;

            public Builder paymentId(Long paymentId) { this.paymentId = paymentId; return this; }
            public Builder billId(Long billId) { this.billId = billId; return this; }
            public Builder amount(Double amount) { this.amount = amount; return this; }
            public Builder paymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; return this; }
            public Builder paymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; return this; }
            public Builder transactionReference(String transactionReference) { this.transactionReference = transactionReference; return this; }
            public Builder status(String status) { this.status = status; return this; }

            public PaymentResponse build() {
                return new PaymentResponse(paymentId, billId, amount, paymentDate, paymentMethod, transactionReference, status);
            }
        }
    }
}
