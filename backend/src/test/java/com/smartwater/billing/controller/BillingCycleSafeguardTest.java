package com.smartwater.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwater.billing.common.entity.BillingCycle;
import com.smartwater.billing.owner.entity.Building;
import com.smartwater.billing.owner.entity.BuildingOwner;
import com.smartwater.billing.common.entity.User;
import com.smartwater.billing.common.exception.BadRequestException;
import com.smartwater.billing.common.repository.BillingCycleRepository;
import com.smartwater.billing.owner.repository.BuildingOwnerRepository;
import com.smartwater.billing.owner.repository.BuildingRepository;
import com.smartwater.billing.common.repository.UserRepository;
import com.smartwater.billing.common.service.BillingCycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:cycle_safeguard_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;NON_KEYWORDS=MONTH,YEAR",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never",
    "spring.mail.host=localhost"
})
public class BillingCycleSafeguardTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BillingCycleRepository billingCycleRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private BuildingOwnerRepository ownerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillingCycleService billingCycleService;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private com.smartwater.billing.common.config.DataInitializer dataInitializer;

    private Building building1;
    private Building building2;
    private User ownerUser1;
    private User ownerUser2;

    @BeforeEach
    void setUp() {
        billingCycleRepository.deleteAll();
        buildingRepository.deleteAll();
        ownerRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Owner 1 & Building 1 (Alpha Heights)
        User u1 = new User(null, "Owner Alice", "owner.alice@test.com", "pass123", User.Role.BUILDING_OWNER, User.UserStatus.ACTIVE, null);
        BuildingOwner owner1 = new BuildingOwner(null, u1, "Owner Alice", "9876543210", BuildingOwner.ApprovalStatus.APPROVED, null);
        owner1 = ownerRepository.save(owner1);
        ownerUser1 = owner1.getUser();

        building1 = new Building(null, owner1, "Alpha Heights", "T1", 50, "Downtown", null);
        building1 = buildingRepository.save(building1);

        // Setup Owner 2 & Building 2 (Beta Plaza)
        User u2 = new User(null, "Owner Bob", "owner.bob@test.com", "pass456", User.Role.BUILDING_OWNER, User.UserStatus.ACTIVE, null);
        BuildingOwner owner2 = new BuildingOwner(null, u2, "Owner Bob", "9876543211", BuildingOwner.ApprovalStatus.APPROVED, null);
        owner2 = ownerRepository.save(owner2);
        ownerUser2 = owner2.getUser();

        building2 = new Building(null, owner2, "Beta Plaza", "T2", 30, "Uptown", null);
        building2 = buildingRepository.save(building2);
    }

    @Test
    @DisplayName("Safeguard 1: Missing buildingId must be rejected with 400 Bad Request and no silent fallback")
    void testMissingBuildingId_RejectedWith400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("startDate", "2026-11-01");
        body.put("endDate", "2026-11-30");

        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("buildingId is required. Silent building selection fallback has been disabled.")));

        // Empty string buildingId
        body.put("buildingId", "   ");
        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("buildingId is required. Silent building selection fallback has been disabled.")));

        // Verify no cycle was created
        assertEquals(0, billingCycleRepository.count());
    }

    @Test
    @DisplayName("Safeguard 2: Non-numeric buildingId must be rejected with 400 Bad Request")
    void testInvalidNumericBuildingId_RejectedWith400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("buildingId", "abc_not_a_number");
        body.put("startDate", "2026-11-01");
        body.put("endDate", "2026-11-30");

        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid numeric buildingId: 'abc_not_a_number'.")));

        assertEquals(0, billingCycleRepository.count());
    }

    @Test
    @DisplayName("Safeguard 3: Non-existent buildingId must return 404 ResourceNotFoundException")
    void testNonExistentBuildingId_Returns404() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("buildingId", 999999L);
        body.put("startDate", "2026-11-01");
        body.put("endDate", "2026-11-30");

        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Building not found with id: 999999")));

        assertEquals(0, billingCycleRepository.count());
    }

    @Test
    @DisplayName("Safeguard 4: Unauthorized owner attempting to create cycle on another owner's building must be rejected with 403 Forbidden")
    void testUnauthorizedBuildingAccess_RejectedWith403() throws Exception {
        // Owner 1 attempts to create cycle on Building 2 (owned by Owner 2)
        Map<String, Object> body = new HashMap<>();
        body.put("buildingId", building2.getBuildingId());
        body.put("startDate", "2026-11-01");
        body.put("endDate", "2026-11-30");

        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString("Access Denied: You do not own building 'Beta Plaza'.")));

        assertEquals(0, billingCycleRepository.count());
    }

    @Test
    @DisplayName("Safeguard 5: Authorized creation succeeds and duplicate cycle creation is rejected safely")
    void testNormalCreationAndDuplicateRejection() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("buildingId", building1.getBuildingId());
        body.put("startDate", "2026-11-01");
        body.put("endDate", "2026-11-30");

        // 1. Initial valid creation
        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.startDate").value("2026-11-01"))
                .andExpect(jsonPath("$.endDate").value("2026-11-30"));

        assertEquals(1, billingCycleRepository.count());

        // 2. Duplicate creation attempt for the same building and month
        mockMvc.perform(post("/billing-cycles")
                .with(user(ownerUser1.getEmail()).roles("BUILDING_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("A billing cycle already exists for building 'Alpha Heights' in NOVEMBER 2026")));

        // Verify count remains 1
        assertEquals(1, billingCycleRepository.count());
    }

    @Test
    @DisplayName("Safeguard 6: SuperAdmin is authorized to create cycles for any valid building")
    void testSuperAdminAuthorizedCreation() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("buildingId", building2.getBuildingId());
        body.put("startDate", "2026-12-01");
        body.put("endDate", "2026-12-31");

        mockMvc.perform(post("/billing-cycles")
                .with(user("superadmin@smartwater.com").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.startDate").value("2026-12-01"))
                .andExpect(jsonPath("$.endDate").value("2026-12-31"));

        assertEquals(1, billingCycleRepository.count());
    }

    @Test
    @DisplayName("Safeguard 7: Database unique constraint uk_building_billing_month rejects concurrent duplicate cycles safely")
    void testConcurrentDatabaseConstraintSafety() {
        // Create first cycle via service
        LocalDate startDate = LocalDate.of(2027, 2, 1);
        LocalDate endDate = LocalDate.of(2027, 2, 28);
        BillingCycle cycle1 = billingCycleService.openCycle(building1.getBuildingId(), startDate, endDate);
        assertNotNull(cycle1.getId());

        // Simulate concurrent insert hitting the unique constraint uk_building_billing_month
        BillingCycle duplicate = BillingCycle.builder()
                .building(building1)
                .startDate(startDate)
                .endDate(endDate)
                .billingMonth(startDate.withDayOfMonth(1))
                .status(BillingCycle.CycleStatus.OPEN)
                .build();

        // Direct flush throws DataIntegrityViolationException due to uk_building_billing_month
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> {
            billingCycleRepository.saveAndFlush(duplicate);
        });
    }
}
