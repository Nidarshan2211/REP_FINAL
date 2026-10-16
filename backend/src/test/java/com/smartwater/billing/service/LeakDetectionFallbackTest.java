package com.smartwater.billing.service;

import com.smartwater.billing.common.dto.LeakDTOs.MlScoreRequest;
import com.smartwater.billing.common.entity.*;
import com.smartwater.billing.common.ml.*;
import com.smartwater.billing.common.repository.*;
import com.smartwater.billing.owner.entity.Apartment;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.owner.repository.ApartmentRepository;
import com.smartwater.billing.owner.repository.BuildingOwnerRepository;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.resident.entity.Resident;
import com.smartwater.billing.resident.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("h2")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:leak_fallback_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;NON_KEYWORDS=MONTH,YEAR",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.mail.host=localhost",
    "ml.enabled=true",
    "ml.api-key=test-key"
})
@Transactional
public class LeakDetectionFallbackTest {

    @Autowired
    private LeakDetectionService leakDetectionService;

    @MockBean
    private MlClient mlClient;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private BuildingOwnerRepository ownerRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private WaterMeterRepository meterRepository;

    @Autowired
    private WaterUsageRepository usageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillingCycleRepository billingCycleRepository;

    private Building building;
    private Apartment apt101;
    private WaterMeter meter101;
    private WaterMeter commonMeter;
    private BillingCycle currentCycle;

    @BeforeEach
    void setUp() {
        // Ensure ML client fails/returns empty to trigger statistical fallback
        when(mlClient.isOperational()).thenReturn(true);
        when(mlClient.scoreBatch(any(MlScoreRequest.class))).thenReturn(Optional.empty());

        User ownerUser = userRepository.save(User.builder()
                .name("Owner Fallback")
                .email("owner.fallback@example.com")
                .password("encoded_pass")
                .role(User.Role.BUILDING_OWNER)
                .status(User.UserStatus.ACTIVE)
                .build());

        BuildingOwner owner = ownerRepository.save(BuildingOwner.builder()
                .user(ownerUser)
                .name("Owner Fallback")
                .phone("9998887771")
                .approvalStatus(BuildingOwner.ApprovalStatus.APPROVED)
                .build());

        building = buildingRepository.save(Building.builder()
                .owner(owner)
                .buildingName("Fallback Heights")
                .buildingNumber("FH-01")
                .location("Central Block")
                .totalFlats(10)
                .build());

        apt101 = apartmentRepository.save(Apartment.builder()
                .building(building)
                .roomNumber("101")
                .floorNumber(1)
                .unitType("2BHK")
                .apartmentName("Flat 101")
                .build());

        Resident resident = residentRepository.save(Resident.builder()
                .name("Resident 101")
                .email("res101@example.com")
                .phone("9876543210")
                .apartmentNumber("101")
                .building(building)
                .build());

        meter101 = meterRepository.save(WaterMeter.builder()
                .building(building)
                .apartment(apt101)
                .resident(resident)
                .meterNumber("WM-FH-101")
                .installationDate(LocalDate.now().minusMonths(6))
                .status(WaterMeter.MeterStatus.ACTIVE)
                .build());

        // Common area meter (apartment is null)
        commonMeter = meterRepository.save(WaterMeter.builder()
                .building(building)
                .apartment(null)
                .resident(null)
                .meterNumber("WM-FH-COMMON")
                .installationDate(LocalDate.now().minusMonths(6))
                .status(WaterMeter.MeterStatus.ACTIVE)
                .build());

        currentCycle = billingCycleRepository.save(BillingCycle.builder()
                .building(building)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .billingMonth(LocalDate.now().withDayOfMonth(1))
                .status(BillingCycle.CycleStatus.OPEN)
                .build());
    }

    @Test
    @DisplayName("0-cycle history: Flat receives INSUFFICIENT_HISTORY on statistical fallback")
    void testZeroCycleHistoryReceivesInsufficientHistory() {
        WaterUsage currentUsage = usageRepository.save(WaterUsage.builder()
                .meter(meter101)
                .billingCycle(currentCycle)
                .month(LocalDate.now().getMonthValue())
                .year(LocalDate.now().getYear())
                .previousReading(0.0)
                .currentReading(12.5)
                .unitsConsumed(12.5)
                .recordedAt(LocalDateTime.now())
                .build());

        leakDetectionService.scoreBillingCycle(building.getBuildingId(), currentCycle.getId());

        WaterUsage scored = usageRepository.findById(currentUsage.getUsageId()).orElseThrow();
        assertEquals("INSUFFICIENT_HISTORY", scored.getAnomalyStatus());
        assertEquals(0.0, scored.getAnomalyScore());
        assertEquals("FALLBACK_INSUFFICIENT_HISTORY", scored.getDataQualityFlag());
    }

    @Test
    @DisplayName("1-cycle history: Flat receives INSUFFICIENT_HISTORY on statistical fallback")
    void testSingleCycleHistoryReceivesInsufficientHistory() {
        // Only 1 past record
        usageRepository.save(WaterUsage.builder()
                .meter(meter101)
                .month(LocalDate.now().minusMonths(1).getMonthValue())
                .year(LocalDate.now().minusMonths(1).getYear())
                .previousReading(0.0)
                .currentReading(10.0)
                .unitsConsumed(10.0)
                .recordedAt(LocalDateTime.now().minusMonths(1))
                .build());

        WaterUsage currentUsage = usageRepository.save(WaterUsage.builder()
                .meter(meter101)
                .billingCycle(currentCycle)
                .month(LocalDate.now().getMonthValue())
                .year(LocalDate.now().getYear())
                .previousReading(10.0)
                .currentReading(25.0)
                .unitsConsumed(15.0)
                .recordedAt(LocalDateTime.now())
                .build());

        leakDetectionService.scoreBillingCycle(building.getBuildingId(), currentCycle.getId());

        WaterUsage scored = usageRepository.findById(currentUsage.getUsageId()).orElseThrow();
        assertEquals("INSUFFICIENT_HISTORY", scored.getAnomalyStatus());
        assertEquals("FALLBACK_INSUFFICIENT_HISTORY", scored.getDataQualityFlag());
    }

    @Test
    @DisplayName(">=2 cycles: Abnormal usage > mean + 2*SD flags POTENTIAL_LEAK")
    void testMultiCycleHistoryFlagsPotentialLeak() {
        // 3 past cycles of ~10 kL
        for (int i = 3; i >= 1; i--) {
            usageRepository.save(WaterUsage.builder()
                    .meter(meter101)
                    .month(LocalDate.now().minusMonths(i).getMonthValue())
                    .year(LocalDate.now().minusMonths(i).getYear())
                    .previousReading((3 - i) * 10.0)
                    .currentReading((4 - i) * 10.0)
                    .unitsConsumed(10.0)
                    .recordedAt(LocalDateTime.now().minusMonths(i))
                    .build());
        }

        // Current cycle: 35 kL (huge sudden spike!)
        WaterUsage currentUsage = usageRepository.save(WaterUsage.builder()
                .meter(meter101)
                .billingCycle(currentCycle)
                .month(LocalDate.now().getMonthValue())
                .year(LocalDate.now().getYear())
                .previousReading(30.0)
                .currentReading(65.0)
                .unitsConsumed(35.0)
                .recordedAt(LocalDateTime.now())
                .build());

        leakDetectionService.scoreBillingCycle(building.getBuildingId(), currentCycle.getId());

        WaterUsage scored = usageRepository.findById(currentUsage.getUsageId()).orElseThrow();
        assertEquals("POTENTIAL_LEAK", scored.getAnomalyStatus());
        assertTrue(scored.getAnomalyScore() >= 0.70);
        assertEquals("SUDDEN_SPIKE", scored.getAnomalyPattern());
        assertEquals("FALLBACK_STATISTICAL", scored.getDataQualityFlag());
        assertNotNull(scored.getIncidentId());
    }

    @Test
    @DisplayName("Common area meter: Excluded from flat scoring, assigned NOT_APPLICABLE")
    void testCommonAreaMeterExcludedFromResidentialScoring() {
        WaterUsage commonUsage = usageRepository.save(WaterUsage.builder()
                .meter(commonMeter)
                .billingCycle(currentCycle)
                .month(LocalDate.now().getMonthValue())
                .year(LocalDate.now().getYear())
                .previousReading(100.0)
                .currentReading(250.0)
                .unitsConsumed(150.0)
                .recordedAt(LocalDateTime.now())
                .build());

        leakDetectionService.scoreBillingCycle(building.getBuildingId(), currentCycle.getId());

        WaterUsage scored = usageRepository.findById(commonUsage.getUsageId()).orElseThrow();
        assertEquals("NOT_APPLICABLE", scored.getAnomalyStatus());
        assertEquals(0.0, scored.getAnomalyScore());
        assertEquals("COMMON_AREA", scored.getDataQualityFlag());
        assertNull(scored.getIncidentId());
    }
}
