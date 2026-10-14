package com.smartwater.billing.common.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.smartwater.billing.common.entity.Bill;
import com.smartwater.billing.common.entity.Payment;
import com.smartwater.billing.common.entity.WaterUsage;
import com.smartwater.billing.common.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private final PaymentRepository paymentRepository;

    public PdfGeneratorService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    public ByteArrayInputStream generateBillPdf(Bill bill) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        boolean isPaid = bill.getStatus() == Bill.BillStatus.PAID;
        Payment payment = isPaid ? paymentRepository.findByBillBillId(bill.getBillId()).orElse(null) : null;

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0, 95, 115));
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, isPaid ? new BaseColor(40, 167, 69) : new BaseColor(0, 53, 84));
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new BaseColor(0, 95, 115));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.DARK_GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, isPaid ? new BaseColor(40, 167, 69) : new BaseColor(0, 95, 115));
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);
            Font paidStampFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(40, 167, 69));

            // Title
            Paragraph title = new Paragraph("SMART WATER USAGE AND CONSUMER BILLING SYSTEM", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            String docSubtitle = isPaid ? "OFFICIAL WATER BILL PAYMENT RECEIPT" : "OFFICIAL WATER CONSUMPTION INVOICE";
            Paragraph subtitle = new Paragraph(docSubtitle, subHeaderFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // 1. Resident & Billing Info Table (2 Columns)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            // Resident Info Box
            PdfPCell resCell = new PdfPCell();
            resCell.setPadding(8);
            resCell.setBorderColor(new BaseColor(212, 235, 248));
            resCell.setBackgroundColor(new BaseColor(240, 250, 255));
            resCell.addElement(new Paragraph("RESIDENT INFORMATION", sectionTitleFont));
            resCell.addElement(new Paragraph("Resident Name: " + (bill.getResident() != null ? bill.getResident().getName() : "N/A"), valueFont));
            resCell.addElement(new Paragraph("Apartment: " + (bill.getResident() != null ? bill.getResident().getApartmentNumber() : "N/A"), valueFont));
            resCell.addElement(new Paragraph("Building: " + (bill.getResident() != null && bill.getResident().getBuilding() != null ? bill.getResident().getBuilding().getBuildingName() : "N/A"), valueFont));
            
            WaterUsage usage = bill.getWaterUsage();
            String meterNum = usage != null && usage.getMeter() != null ? usage.getMeter().getMeterNumber() : "WM-DEFAULT";
            resCell.addElement(new Paragraph("Water Meter #: " + meterNum, valueFont));
            infoTable.addCell(resCell);

            // Billing Info Box
            PdfPCell billCell = new PdfPCell();
            billCell.setPadding(8);
            billCell.setBorderColor(new BaseColor(212, 235, 248));
            billCell.setBackgroundColor(new BaseColor(240, 250, 255));
            billCell.addElement(new Paragraph("BILLING & PAYMENT DETAILS", sectionTitleFont));
            billCell.addElement(new Paragraph("Invoice / Receipt #: SWB-" + bill.getBillId(), valueFont));
            
            String cyclePeriod = bill.getBillingCycle() != null ? 
                    bill.getBillingCycle().getStartDate() + " to " + bill.getBillingCycle().getEndDate() : 
                    bill.getBillMonth() + "/" + bill.getBillYear();
            billCell.addElement(new Paragraph("Billing Period: " + cyclePeriod, valueFont));
            billCell.addElement(new Paragraph("Due Date: " + (bill.getDueDate() != null ? bill.getDueDate() : "N/A"), valueFont));
            billCell.addElement(new Paragraph("Status: " + bill.getStatus().name() + (isPaid ? " (CONFIRMED)" : ""), valueFont));
            infoTable.addCell(billCell);

            document.add(infoTable);

            // PAYMENT RECEIPT STAMP BOX IF PAID
            if (isPaid) {
                PdfPTable receiptBox = new PdfPTable(1);
                receiptBox.setWidthPercentage(100);
                receiptBox.setSpacingAfter(15);

                PdfPCell rCell = new PdfPCell();
                rCell.setPadding(10);
                rCell.setBorderColor(new BaseColor(40, 167, 69));
                rCell.setBackgroundColor(new BaseColor(235, 247, 238));
                
                rCell.addElement(new Paragraph("✓ PAYMENT RECEIVED - RECEIPT STAMP", paidStampFont));
                
                String pMethod = payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "RAZORPAY_PAYMENT";
                String pRef = payment != null && payment.getTransactionReference() != null ? payment.getTransactionReference() : "rzp_pay_confirmed";
                String pDate = payment != null && payment.getPaymentDate() != null ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A";
                
                rCell.addElement(new Paragraph("Payment Method: " + pMethod, valueFont));
                rCell.addElement(new Paragraph("Razorpay Reference ID: " + pRef, valueFont));
                rCell.addElement(new Paragraph("Payment Timestamp: " + pDate, valueFont));
                rCell.addElement(new Paragraph("Total Paid: INR " + String.format("%.2f", bill.getAmount()), valueFont));

                receiptBox.addCell(rCell);
                document.add(receiptBox);
            }

            // 2. Consumption Breakdown Section
            Paragraph consHeader = new Paragraph("WATER METER CONSUMPTION DETAILS", sectionTitleFont);
            consHeader.setSpacingAfter(8);
            document.add(consHeader);

            PdfPTable consTable = new PdfPTable(3);
            consTable.setWidthPercentage(100);
            consTable.setSpacingAfter(15);
            
            addHeaderCell(consTable, "Previous Reading", labelFont);
            addHeaderCell(consTable, "Current Reading", labelFont);
            addHeaderCell(consTable, "Total Consumption (kL)", labelFont);

            double prevRead = usage != null && usage.getPreviousReading() != null ? usage.getPreviousReading() : 0.0;
            double currRead = usage != null && usage.getCurrentReading() != null ? usage.getCurrentReading() : (bill.getUnits() != null ? bill.getUnits() : 0.0);
            double totalUnits = bill.getUnits() != null ? bill.getUnits() : 0.0;

            addBodyCell(consTable, String.format("%.2f", prevRead), valueFont);
            addBodyCell(consTable, String.format("%.2f", currRead), valueFont);
            addBodyCell(consTable, String.format("%.2f kL", totalUnits), valueFont);

            document.add(consTable);

            // 3. Tariff Breakdown Section
            Paragraph tariffHeader = new Paragraph("ITEMIZED 3-TIER PROGRESSIVE TARIFF BREAKDOWN", sectionTitleFont);
            tariffHeader.setSpacingAfter(8);
            document.add(tariffHeader);

            PdfPTable tariffTable = new PdfPTable(4);
            tariffTable.setWidthPercentage(100);
            tariffTable.setSpacingAfter(15);

            addHeaderCell(tariffTable, "Slab Tier", labelFont);
            addHeaderCell(tariffTable, "Units Consumed (kL)", labelFont);
            addHeaderCell(tariffTable, "Rate (INR / kL)", labelFont);
            addHeaderCell(tariffTable, "Subtotal Amount (INR)", labelFont);

            BigDecimal t1Units = bill.getTier1Units() != null ? bill.getTier1Units() : BigDecimal.ZERO;
            BigDecimal t1Rate = bill.getTier1Rate() != null ? bill.getTier1Rate() : new BigDecimal("2.50");
            BigDecimal t1Amount = bill.getTier1Amount() != null ? bill.getTier1Amount() : BigDecimal.ZERO;

            BigDecimal t2Units = bill.getTier2Units() != null ? bill.getTier2Units() : BigDecimal.ZERO;
            BigDecimal t2Rate = bill.getTier2Rate() != null ? bill.getTier2Rate() : new BigDecimal("4.00");
            BigDecimal t2Amount = bill.getTier2Amount() != null ? bill.getTier2Amount() : BigDecimal.ZERO;

            BigDecimal t3Units = bill.getTier3Units() != null ? bill.getTier3Units() : BigDecimal.ZERO;
            BigDecimal t3Rate = bill.getTier3Rate() != null ? bill.getTier3Rate() : new BigDecimal("6.00");
            BigDecimal t3Amount = bill.getTier3Amount() != null ? bill.getTier3Amount() : BigDecimal.ZERO;

            addBodyCell(tariffTable, "Tier 1 (0-20 kL)", valueFont);
            addBodyCell(tariffTable, t1Units.toString() + " kL", valueFont);
            addBodyCell(tariffTable, "INR " + t1Rate.toString(), valueFont);
            addBodyCell(tariffTable, "INR " + t1Amount.toString(), valueFont);

            addBodyCell(tariffTable, "Tier 2 (21-40 kL)", valueFont);
            addBodyCell(tariffTable, t2Units.toString() + " kL", valueFont);
            addBodyCell(tariffTable, "INR " + t2Rate.toString(), valueFont);
            addBodyCell(tariffTable, "INR " + t2Amount.toString(), valueFont);

            addBodyCell(tariffTable, "Tier 3 (>40 kL)", valueFont);
            addBodyCell(tariffTable, t3Units.toString() + " kL", valueFont);
            addBodyCell(tariffTable, "INR " + t3Rate.toString(), valueFont);
            addBodyCell(tariffTable, "INR " + t3Amount.toString(), valueFont);

            document.add(tariffTable);

            // 4. Total Summary Box
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setSpacingAfter(20);

            PdfPCell totalLabelCell = new PdfPCell(new Phrase(isPaid ? "TOTAL AMOUNT PAID:" : "TOTAL AMOUNT DUE:", totalFont));
            totalLabelCell.setBorder(Rectangle.NO_BORDER);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.addCell(totalLabelCell);

            PdfPCell totalValCell = new PdfPCell(new Phrase(String.format("INR %.2f", bill.getAmount()), totalFont));
            totalValCell.setBorder(Rectangle.NO_BORDER);
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.addCell(totalValCell);

            document.add(totalTable);

            // Footer
            Paragraph footer = new Paragraph(isPaid ?
                    "Paid Receipt Confirmed. Thank you for your payment and conserving water! | AquaFlow" :
                    "Thank you for conserving water! | Smart Water Usage & Consumer Billing System", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF receipt", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new BaseColor(227, 246, 255));
        cell.setPadding(6);
        cell.setBorderColor(new BaseColor(212, 235, 248));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(new BaseColor(232, 244, 250));
        table.addCell(cell);
    }
}
