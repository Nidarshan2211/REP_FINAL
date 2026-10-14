package com.smartwater.billing.service;

import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.owner.entity.*;
import com.smartwater.billing.resident.entity.*;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.repository.*;
import com.smartwater.billing.resident.repository.*;
import com.smartwater.billing.owner.service.BulkBillingService;
import com.smartwater.billing.common.service.BillingCycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("h2")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:bulk_rollback_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;NON_KEYWORDS=MONTH,YEAR",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.mail.host=localhost"
})
public class BulkBillingRollbackIntegrationTest {

    @Autowired
    private BulkBillingService bulkBillingService;

    @Autowired
    private BillingCycleRepository billingCycleRepository;

    @Autowired
    private WaterUsageRepository waterUsageRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

    @SpyBean
    private BillingCycleService billingCycleService;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private com.smartwater.billing.common.config.DataInitializer dataInitializer;

    private Building building;
    private BuildingOwner owner;
    private BillingCycle cycle;
    private Resident resident1, resident2, resident3, resident4;
    private WaterMeter meter1, meter2, meter3, meter4;

    @BeforeEach
    void setupIsolatedTestData() {
        notificationRepository.deleteAll();
        invoiceRepository.deleteAll();
        billRepository.deleteAll();
        waterUsageRepository.deleteAll();
        billingCycleRepository.deleteAll();
        waterMeterRepository.deleteAll();
        residentRepository.deleteAll();
        buildingRepository.deleteAll();
        ownerRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create Building Owner
        User ownerUser = new User(null, "Owner Robert", "owner.robert@gmail.com", "pass123", User.Role.BUILDING_OWNER, User.UserStatus.ACTIVE, null);
        owner = new BuildingOwner(null, ownerUser, "Owner Robert", "9876543210", BuildingOwner.ApprovalStatus.APPROVED, null);
        owner = ownerRepository.save(owner);

        // 2. Create Building
        building = new Building(null, owner, "Sandbox Tower", "B-03", 4, "Metropolis", null);
        building = buildingRepository.save(building);

        // 3. Create 4 Residents
        resident1 = residentRepository.save(Resident.builder()
                .building(building).apartmentNumber("QA-101").name("QA Resident 1")
                .email("qa1@test.com").phone("9876543211").accountStatus(Resident.AccountStatus.ACTIVE)
                .invitationStatus(Resident.InvitationStatus.ACCEPTED).build());
        resident2 = residentRepository.save(Resident.builder()
                .building(building).apartmentNumber("QA-102").name("QA Resident 2")
                .email("qa2@test.com").phone("9876543212").accountStatus(Resident.AccountStatus.ACTIVE)
                .invitationStatus(Resident.InvitationStatus.ACCEPTED).build());
        resident3 = residentRepository.save(Resident.builder()
                .building(building).apartmentNumber("QA-103").name("QA Resident 3")
                .email("qa3@test.com").phone("9876543213").accountStatus(Resident.AccountStatus.ACTIVE)
                .invitationStatus(Resident.InvitationStatus.ACCEPTED).build());
        resident4 = residentRepository.save(Resident.builder()
                .building(building).apartmentNumber("QA-104").name("QA Resident 4")
                .email("qa4@test.com").phone("9876543214").accountStatus(Resident.AccountStatus.ACTIVE)
                .invitationStatus(Resident.InvitationStatus.ACCEPTED).build());

        // 4. Create 4 Active Water Meters
        meter1 = waterMeterRepository.save(new WaterMeter(null, building, resident1, "MTR-101", LocalDate.now(), WaterMeter.MeterStatus.ACTIVE));
        meter2 = waterMeterRepository.save(new WaterMeter(null, building, resident2, "MTR-102", LocalDate.now(), WaterMeter.MeterStatus.ACTIVE));
        meter3 = waterMeterRepository.save(new WaterMeter(null, building, resident3, "MTR-103", LocalDate.now(), WaterMeter.MeterStatus.ACTIVE));
        meter4 = waterMeterRepository.save(new WaterMeter(null, building, resident4, "MTR-104", LocalDate.now(), WaterMeter.MeterStatus.ACTIVE));

        // 5. Create OPEN Billing Cycle
        cycle = BillingCycle.builder()
                .building(building)
                .billingMonth(LocalDate.now().withDayOfMonth(1))
                .startDate(LocalDate.now().withDayOfMonth(1))
                .endDate(LocalDate.now().withDayOfMonth(28))
                .status(BillingCycle.CycleStatus.OPEN)
                .build();
        cycle = billingCycleRepository.save(cycle);
    }

    @Test
    @DisplayName("Verify mid-transaction exception after writes begun triggers complete DB rollback and suppresses email")
    void testMidTransactionExceptionRollsBackAllWrites() {
        Long buildingId = building.getBuildingId();
        Long cycleId = cycle.getId();
        String ownerEmail = owner.getUser().getEmail();

        // 1. Baseline Pre-Checks
        BillingCycle cycleBefore = billingCycleRepository.findById(cycleId).orElseThrow();
        assertEquals(BillingCycle.CycleStatus.OPEN, cycleBefore.getStatus(), "Cycle must initially be OPEN");
        assertTrue(waterUsageRepository.findByBillingCycleId(cycleId).isEmpty(), "Pre-condition: 0 water usages for cycle");
        assertTrue(invoiceRepository.findByBillingCycleId(cycleId).isEmpty(), "Pre-condition: 0 invoices for cycle");
        assertFalse(billRepository.findByBillingCycleIdAndResidentResidentId(cycleId, resident1.getResidentId()).isPresent());

        // 2. Prepare Valid CSV payload covering all 4 residents
        String csvContent = "resident_id,apartment_number,meter_id,previous_reading,current_reading\n" +
                resident1.getResidentId() + ",QA-101," + meter1.getMeterId() + ",0.00,10.00\n" +
                resident2.getResidentId() + ",QA-102," + meter2.getMeterId() + ",0.00,20.00\n" +
                resident3.getResidentId() + ",QA-103," + meter3.getMeterId() + ",0.00,30.00\n" +
                resident4.getResidentId() + ",QA-104," + meter4.getMeterId() + ",0.00,50.00\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "valid_meter_readings.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // 3. Inject Exception in finalizeCycle AFTER BulkBillingService has executed usageRepository.saveAll writes
        doAnswer(invocation -> {
            throw new RuntimeException("Controlled test injection: Catastrophic crash in finalizeCycle after usageRepository.saveAll writes");
        }).when(billingCycleService).finalizeCycle(eq(cycleId));

        // 4. Execute generateBills - must propagate RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            bulkBillingService.generateBills(ownerEmail, buildingId, cycleId, file);
        }, "Should propagate the injected RuntimeException from transaction boundary");

        assertTrue(thrown.getMessage().contains("Controlled test injection"));

        // 5. DIRECT DATABASE ASSERTIONS (Proving physical database rollback in isolated H2):
        List<WaterUsage> postUsages = waterUsageRepository.findByBillingCycleId(cycleId);
        assertEquals(0, postUsages.size(), "Physical DB check: Attempted water_usage database writes MUST be 100% rolled back");

        assertFalse(billRepository.findByBillingCycleIdAndResidentResidentId(cycleId, resident1.getResidentId()).isPresent());
        assertFalse(billRepository.findByBillingCycleIdAndResidentResidentId(cycleId, resident2.getResidentId()).isPresent());
        assertFalse(billRepository.findByBillingCycleIdAndResidentResidentId(cycleId, resident3.getResidentId()).isPresent());
        assertFalse(billRepository.findByBillingCycleIdAndResidentResidentId(cycleId, resident4.getResidentId()).isPresent());

        List<?> postInvoices = invoiceRepository.findByBillingCycleId(cycleId);
        assertEquals(0, postInvoices.size(), "Physical DB check: Invoices MUST be 0");

        assertTrue(notificationRepository.findByResidentResidentIdOrderByCreatedAtDesc(resident1.getResidentId()).isEmpty());

        BillingCycle cycleAfter = billingCycleRepository.findById(cycleId).orElseThrow();
        assertEquals(BillingCycle.CycleStatus.OPEN, cycleAfter.getStatus(), "Physical DB check: Cycle status MUST remain OPEN after rollback");

        // 6. EXTERNAL EMAIL SUPPRESSION ASSERTION:
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
