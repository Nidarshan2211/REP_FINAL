package com.smartwater.billing.service;

import com.smartwater.billing.owner.dto.BulkBillingDTOs.BulkImportResponse;
import com.smartwater.billing.owner.dto.BulkBillingDTOs.BulkValidationResponse;
import com.smartwater.billing.owner.dto.BulkBillingDTOs.RowStatus;
import com.smartwater.billing.common.dto.UsageDTOs.RecordUsageRequest;
import com.smartwater.billing.common.dto.UsageDTOs.UsageResponse;
import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.owner.service.BulkBillingService;
import com.smartwater.billing.common.service.WaterUsageService;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:csv_reading_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;NON_KEYWORDS=MONTH,YEAR",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.mail.host=localhost"
})
public class CsvMeterReadingInputTest {

    @Autowired
    private BulkBillingService bulkBillingService;

    @Autowired
    private WaterUsageService waterUsageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuildingOwnerRepository ownerRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private WaterMeterRepository waterMeterRepository;

    @Autowired
    private BillingCycleRepository billingCycleRepository;

    @Autowired
    private WaterUsageRepository waterUsageRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private TariffPlanRepository tariffPlanRepository;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private com.smartwater.billing.common.config.DataInitializer dataInitializer;

    private Building building;
    private BuildingOwner owner;
    private BillingCycle cycle;
    private Resident resident1;
    private Resident resident2;
    private WaterMeter meter1;
    private WaterMeter meter2;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        billRepository.deleteAll();
        waterUsageRepository.deleteAll();
        tariffPlanRepository.deleteAll();
        billingCycleRepository.deleteAll();
        waterMeterRepository.deleteAll();
        residentRepository.deleteAll();
        buildingRepository.deleteAll();
        ownerRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Owner & Building
        User ownerUser = new User(null, "Owner Robert", "owner.robert@test.com", "pass123", User.Role.BUILDING_OWNER, User.UserStatus.ACTIVE, null);
        owner = new BuildingOwner(null, ownerUser, "Owner Robert", "9876543210", BuildingOwner.ApprovalStatus.APPROVED, null);
        owner = ownerRepository.save(owner);

        building = new Building(null, owner, "Sandbox Tower", "SB-01", 10, "Sandbox City", null);
        building = buildingRepository.save(building);

        // 2. Setup Residents
        resident1 = Resident.builder()
                .building(building)
                .name("Resident Alice")
                .email("alice@test.com")
                .phone("9876543211")
                .apartmentNumber("101")
                .invitationStatus(Resident.InvitationStatus.ACCEPTED)
                .accountStatus(Resident.AccountStatus.ACTIVE)
                .build();
        resident1 = residentRepository.save(resident1);

        resident2 = Resident.builder()
                .building(building)
                .name("Resident Bob")
                .email("bob@test.com")
                .phone("9876543212")
                .apartmentNumber("102")
                .invitationStatus(Resident.InvitationStatus.ACCEPTED)
                .accountStatus(Resident.AccountStatus.ACTIVE)
                .build();
        resident2 = residentRepository.save(resident2);

        // 3. Setup Water Meters
        meter1 = WaterMeter.builder()
                .building(building)
                .resident(resident1)
                .meterNumber("WM-SB-101")
                .installationDate(LocalDate.now())
                .status(WaterMeter.MeterStatus.ACTIVE)
                .build();
        meter1 = waterMeterRepository.save(meter1);

        meter2 = WaterMeter.builder()
                .building(building)
                .resident(resident2)
                .meterNumber("WM-SB-102")
                .installationDate(LocalDate.now())
                .status(WaterMeter.MeterStatus.ACTIVE)
                .build();
        meter2 = waterMeterRepository.save(meter2);

        // 4. Setup OPEN Billing Cycle
        cycle = BillingCycle.builder()
                .building(building)
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 11, 30))
                .billingMonth(LocalDate.of(2026, 11, 1))
                .status(BillingCycle.CycleStatus.OPEN)
                .build();
        cycle = billingCycleRepository.save(cycle);
    }

    @Test
    @DisplayName("Test 1: Valid CSV Upload imports readings, keeps cycle OPEN, and creates 0 bills or invoices")
    void testValidCsvUpload_ImportsReadingsAndPreservesOpenCycle() {
        String csvContent = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,0.00,15.50\n" +
                resident2.getResidentId() + ",102,Resident Bob," + meter2.getMeterId() + ",WM-SB-102,0.00,22.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "valid_readings.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Step 1: Preview & Validate
        BulkValidationResponse preview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file);
        assertEquals(2, preview.getValidCount());
        assertEquals(0, preview.getErrorCount());
        assertEquals(37.50, preview.getTotalEstimatedUnits(), 0.01);

        // Step 2: Submit / Import Readings
        BulkImportResponse response = bulkBillingService.importReadings(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file);

        assertNotNull(response);
        assertEquals(0, response.getBillsGeneratedCount(), "Must generate 0 bills");
        assertFalse(response.isCycleFinalized(), "Cycle must NOT be finalized");
        assertEquals("OPEN", response.getCycleStatus(), "Cycle must remain OPEN");

        // Verify Database State
        List<WaterUsage> usages = waterUsageRepository.findByBillingCycleId(cycle.getId());
        assertEquals(2, usages.size(), "Must persist exactly 2 usage records");

        WaterUsage usage1 = usages.stream().filter(u -> u.getMeter().getMeterId().equals(meter1.getMeterId())).findFirst().orElseThrow();
        assertEquals(0.00, usage1.getPreviousReading(), 0.01);
        assertEquals(15.50, usage1.getCurrentReading(), 0.01);
        assertEquals(15.50, usage1.getUnitsConsumed(), 0.01);

        WaterUsage usage2 = usages.stream().filter(u -> u.getMeter().getMeterId().equals(meter2.getMeterId())).findFirst().orElseThrow();
        assertEquals(0.00, usage2.getPreviousReading(), 0.01);
        assertEquals(22.00, usage2.getCurrentReading(), 0.01);
        assertEquals(22.00, usage2.getUnitsConsumed(), 0.01);

        // Strict invariant check: Zero bills and Zero invoices
        assertEquals(0, billRepository.count(), "Bills count must be 0");
        assertEquals(0, invoiceRepository.count(), "Invoices count must be 0");

        // Billing cycle status must remain OPEN
        BillingCycle refreshedCycle = billingCycleRepository.findById(cycle.getId()).orElseThrow();
        assertEquals(BillingCycle.CycleStatus.OPEN, refreshedCycle.getStatus());
    }

    @Test
    @DisplayName("Test 2: CSV with Decreasing Reading is rejected and creates 0 records (atomic rollback)")
    void testDecreasingReadingCsv_RejectedWithZeroPartialRecords() {
        String csvContent = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,50.00,20.00\n"; // Decreasing!

        MockMultipartFile file = new MockMultipartFile(
                "file", "decreasing_readings.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Validation detects error
        BulkValidationResponse preview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file);
        assertEquals(1, preview.getErrorCount());
        assertEquals(0, preview.getValidCount());
        assertEquals(RowStatus.INVALID, preview.getRows().get(0).getStatus());
        assertTrue(preview.getRows().get(0).getErrorMessage().contains("Decreasing meter reading"));

        // Import must throw BadRequestException
        assertThrows(BadRequestException.class, () ->
                bulkBillingService.importReadings(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file)
        );

        // Database must remain 100% clean
        assertEquals(0, waterUsageRepository.findByBillingCycleId(cycle.getId()).size());
        assertEquals(0, billRepository.count());
    }

    @Test
    @DisplayName("Test 3: CSV with Missing Header is rejected and creates 0 records")
    void testMissingHeaderCsv_RejectedWithZeroRecords() {
        String csvContent = "apartment_number,meter_number,previous_reading,current_reading\n" + // Missing resident_id!
                "101,WM-SB-101,0.00,10.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "missing_header.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file)
        );
        assertTrue(ex.getMessage().contains("CSV is missing required header: 'resident_id'"));

        assertThrows(BadRequestException.class, () ->
                bulkBillingService.importReadings(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file)
        );

        assertEquals(0, waterUsageRepository.count());
    }

    @Test
    @DisplayName("Test 4: CSV with Negative Reading is rejected and creates 0 records")
    void testNegativeReadingCsv_RejectedWithZeroRecords() {
        String csvContent = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,0.00,-10.00\n"; // Negative!

        MockMultipartFile file = new MockMultipartFile(
                "file", "negative_readings.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        BulkValidationResponse preview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file);
        assertEquals(1, preview.getErrorCount());
        assertTrue(preview.getRows().get(0).getErrorMessage().contains("cannot be negative"));

        assertThrows(BadRequestException.class, () ->
                bulkBillingService.importReadings(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file)
        );

        assertEquals(0, waterUsageRepository.count());
    }

    @Test
    @DisplayName("Test 5: CSV with Duplicate Resident Row is rejected and creates 0 records")
    void testDuplicateResidentCsv_RejectedWithZeroRecords() {
        String csvContent = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,0.00,15.00\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,0.00,20.00\n"; // Duplicate!

        MockMultipartFile file = new MockMultipartFile(
                "file", "duplicate_resident.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        BulkValidationResponse preview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file);
        assertEquals(1, preview.getErrorCount());
        assertEquals(RowStatus.DUPLICATE_ROW, preview.getRows().get(1).getStatus());

        assertThrows(BadRequestException.class, () ->
                bulkBillingService.importReadings(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file)
        );

        assertEquals(0, waterUsageRepository.count());
    }

    @Test
    @DisplayName("Test 6: Manual Input method continues to function properly alongside CSV import")
    void testManualInput_StillWorksProperly() {
        RecordUsageRequest request = new RecordUsageRequest(
                meter1.getMeterId(),
                cycle.getId(),
                11,
                2026,
                0.0,
                18.5
        );

        UsageResponse response = waterUsageService.recordUsage(owner.getUser().getEmail(), request);
        assertNotNull(response);
        assertEquals(18.5, response.getUnitsConsumed(), 0.01);
        assertEquals(18.5, response.getCurrentReading(), 0.01);

        // Verify in DB
        List<WaterUsage> usages = waterUsageRepository.findByBillingCycleId(cycle.getId());
        assertEquals(1, usages.size());
        assertEquals(18.5, usages.get(0).getUnitsConsumed(), 0.01);

        // Verify cycle is still OPEN and 0 bills created
        BillingCycle cycleInDb = billingCycleRepository.findById(cycle.getId()).orElseThrow();
        assertEquals(BillingCycle.CycleStatus.OPEN, cycleInDb.getStatus());
        assertEquals(0, billRepository.count());
    }

    @Test
    @DisplayName("Test 7: Dynamic CSV template generation produces clean reusable template with correct identifiers and empty current_reading")
    void testGenerateTemplate_DynamicAndReusable() throws Exception {
        byte[] csvBytes = bulkBillingService.generateTemplate(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId());
        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        List<List<String>> rows = com.smartwater.billing.common.util.CsvHelper.parseCsv(new java.io.ByteArrayInputStream(csvBytes));
        assertEquals(3, rows.size(), "Should contain 1 header row + 2 resident rows");

        // Header verification
        List<String> headers = rows.get(0);
        assertEquals(List.of("resident_id", "apartment_number", "resident_name", "meter_id", "meter_number", "previous_reading", "current_reading"), headers);

        // Row 1 verification (Resident Alice in 101)
        List<String> row1 = rows.get(1);
        assertEquals(String.valueOf(resident1.getResidentId()), row1.get(0));
        assertEquals("101", row1.get(1));
        assertEquals("Resident Alice", row1.get(2));
        assertEquals(String.valueOf(meter1.getMeterId()), row1.get(3));
        assertEquals("WM-SB-101", row1.get(4));
        assertEquals("0.00", row1.get(5));
        assertEquals("", row1.get(6), "Current reading must be blank for owner to fill in");

        // Row 2 verification (Resident Bob in 102)
        List<String> row2 = rows.get(2);
        assertEquals(String.valueOf(resident2.getResidentId()), row2.get(0));
        assertEquals("102", row2.get(1));
        assertEquals("Resident Bob", row2.get(2));
        assertEquals(String.valueOf(meter2.getMeterId()), row2.get(3));
        assertEquals("WM-SB-102", row2.get(4));
        assertEquals("0.00", row2.get(5));
        assertEquals("", row2.get(6), "Current reading must be blank for owner to fill in");

        // Verify zero database modifications occurred during template generation
        assertEquals(2, residentRepository.count());
        assertEquals(2, waterMeterRepository.count());
        assertEquals(0, waterUsageRepository.count());
    }

    @Test
    @DisplayName("Regression Test: Multi-cycle previous reading mismatch detection and correct cycle template pre-population")
    void testMultiCycle_PreviousReadingMismatch_AndCorrectTemplatePopulation() throws Exception {
        // Step 1: Record Cycle 1 usage for Alice (0.00 -> 124.50) and Bob (0.00 -> 98.00)
        String cycle1Csv = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,0.00,124.50\n" +
                resident2.getResidentId() + ",102,Resident Bob," + meter2.getMeterId() + ",WM-SB-102,0.00,98.00\n";
        MockMultipartFile fileC1 = new MockMultipartFile("file", "c1.csv", "text/csv", cycle1Csv.getBytes(StandardCharsets.UTF_8));
        bulkBillingService.importReadings(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), fileC1);

        // Step 2: Create OPEN Cycle 2
        final BillingCycle cycle2 = billingCycleRepository.save(BillingCycle.builder()
                .building(building)
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .billingMonth(LocalDate.of(2026, 12, 1))
                .status(BillingCycle.CycleStatus.OPEN)
                .build());

        // Step 3: Verify Cycle 2 template generation automatically carries over Cycle 1 ending readings (124.50 and 98.00)
        byte[] c2TemplateBytes = bulkBillingService.generateTemplate(owner.getUser().getEmail(), building.getBuildingId(), cycle2.getId());
        List<List<String>> c2Rows = com.smartwater.billing.common.util.CsvHelper.parseCsv(new java.io.ByteArrayInputStream(c2TemplateBytes));
        assertEquals("124.50", c2Rows.get(1).get(5), "Cycle 2 starting previous_reading for Alice must be 124.50");
        assertEquals("98.00", c2Rows.get(2).get(5), "Cycle 2 starting previous_reading for Bob must be 98.00");

        // Step 4: If owner uploads old Cycle 1 CSV (with previous_reading = 0.00) to Cycle 2, pre-flight validation catches mismatch
        BulkValidationResponse preview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle2.getId(), fileC1);
        assertEquals(2, preview.getErrorCount(), "Both rows must fail previous reading mismatch");
        assertFalse(preview.isCanFinalize());
        assertTrue(preview.getRows().get(0).getErrorMessage().contains("Previous reading mismatch for resident Resident Alice"));
        assertTrue(preview.getRows().get(0).getErrorMessage().contains("Uploaded: 0.00, Trusted Database: 124.50"));

        // Step 5: Finalize call must also strictly reject previous reading mismatch
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bulkBillingService.generateBills(owner.getUser().getEmail(), building.getBuildingId(), cycle2.getId(), fileC1)
        );
        assertTrue(ex.getMessage().contains("Previous reading mismatch for resident Resident Alice"));
        assertTrue(ex.getMessage().contains("Uploaded: 0.00, Trusted Database: 124.50"));

        // Step 6: Uploading valid Cycle 2 CSV (with previous_reading = 124.50 / 98.00 and current = 150.00 / 120.00) passes validation
        String validC2Csv = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter1.getMeterId() + ",WM-SB-101,124.50,150.00\n" +
                resident2.getResidentId() + ",102,Resident Bob," + meter2.getMeterId() + ",WM-SB-102,98.00,120.00\n";
        MockMultipartFile validC2File = new MockMultipartFile("file", "valid_c2.csv", "text/csv", validC2Csv.getBytes(StandardCharsets.UTF_8));
        BulkValidationResponse validPreview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle2.getId(), validC2File);
        assertEquals(2, validPreview.getValidCount());
        assertEquals(0, validPreview.getErrorCount());
        assertTrue(validPreview.isCanFinalize());

        // Step 7: Cycle 1 re-validation: when Cycle 1 already has usage (0.00 -> 124.50), Cycle 1's starting reading is still 0.00
        double cycle1AlicePrev = bulkBillingService.getTrustedPreviousReading(meter1.getMeterId(), cycle.getId());
        assertEquals(0.00, cycle1AlicePrev, 0.001, "Cycle 1 trusted previous reading must remain 0.00 even after readings are recorded");
    }

    @Test
    @DisplayName("Regression Test: Meter already assigned to another resident cannot be hijacked in CSV upload")
    void testCrossResidentMeterHijacking_Rejected() {
        // Attempt to upload Alice's row pointing to Bob's meter2
        String hijackCsv = "resident_id,apartment_number,resident_name,meter_id,meter_number,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",101,Resident Alice," + meter2.getMeterId() + ",WM-SB-102,0.00,50.00\n";
        MockMultipartFile file = new MockMultipartFile("file", "hijack.csv", "text/csv", hijackCsv.getBytes(StandardCharsets.UTF_8));

        BulkValidationResponse preview = bulkBillingService.validateCsv(owner.getUser().getEmail(), building.getBuildingId(), cycle.getId(), file);
        assertEquals(1, preview.getErrorCount());
        assertTrue(preview.getRows().get(0).getErrorMessage().contains("is already assigned to resident Resident Bob"));
    }
}
