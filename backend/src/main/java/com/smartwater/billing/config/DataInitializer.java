package com.smartwater.billing.config;

import com.smartwater.billing.entity.*;
import com.smartwater.billing.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BuildingOwnerRepository ownerRepository;
    private final BuildingRepository buildingRepository;
    private final ResidentRepository residentRepository;
    private final WaterMeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final WaterUsageRepository usageRepository;
    private final BillRepository billRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        String encodedPassword = passwordEncoder.encode("Admin@123456");

        // 1. Ensure Super Admin exists
        User adminUser = userRepository.findByEmail("admin@smartwater.com").orElse(null);
        if (adminUser == null) {
            log.info("Creating Super Admin user...");
            adminUser = User.builder()
                    .name("System Super Admin")
                    .email("admin@smartwater.com")
                    .password(encodedPassword)
                    .role(User.Role.SUPER_ADMIN)
                    .status(User.UserStatus.APPROVED)
                    .build();
            userRepository.save(adminUser);
        } else {
            adminUser.setPassword(encodedPassword);
            adminUser.setStatus(User.UserStatus.APPROVED);
            userRepository.save(adminUser);
        }

        // 2. Ensure Approved Building Owner exists
        User ownerUser1 = userRepository.findByEmail("owner.robert@gmail.com").orElse(null);
        if (ownerUser1 == null) {
            log.info("Creating Approved Owner user...");
            ownerUser1 = User.builder()
                    .name("Robert Sterling")
                    .email("owner.robert@gmail.com")
                    .password(encodedPassword)
                    .role(User.Role.BUILDING_OWNER)
                    .status(User.UserStatus.APPROVED)
                    .build();
            userRepository.save(ownerUser1);

            BuildingOwner owner1 = BuildingOwner.builder()
                    .user(ownerUser1)
                    .name("Robert Sterling")
                    .phone("+1 555-0192")
                    .approvalStatus(BuildingOwner.ApprovalStatus.APPROVED)
                    .build();
            ownerRepository.save(owner1);
        } else {
            ownerUser1.setPassword(encodedPassword);
            ownerUser1.setStatus(User.UserStatus.APPROVED);
            userRepository.save(ownerUser1);
        }

        // 3. Ensure Pending Building Owner exists
        User ownerUser2 = userRepository.findByEmail("owner.sarah@gmail.com").orElse(null);
        if (ownerUser2 == null) {
            log.info("Creating Pending Owner user...");
            ownerUser2 = User.builder()
                    .name("Sarah Jenkins")
                    .email("owner.sarah@gmail.com")
                    .password(encodedPassword)
                    .role(User.Role.BUILDING_OWNER)
                    .status(User.UserStatus.PENDING)
                    .build();
            userRepository.save(ownerUser2);

            BuildingOwner owner2 = BuildingOwner.builder()
                    .user(ownerUser2)
                    .name("Sarah Jenkins")
                    .phone("+1 555-0843")
                    .approvalStatus(BuildingOwner.ApprovalStatus.PENDING)
                    .build();
            ownerRepository.save(owner2);
        } else {
            ownerUser2.setPassword(encodedPassword);
            userRepository.save(ownerUser2);
        }

        // 4. Ensure Active Resident User exists
        User residentUser1 = userRepository.findByEmail("michael.resident@gmail.com").orElse(null);
        if (residentUser1 == null) {
            log.info("Creating Active Resident user...");
            residentUser1 = User.builder()
                    .name("Michael Scott")
                    .email("michael.resident@gmail.com")
                    .password(encodedPassword)
                    .role(User.Role.RESIDENT)
                    .status(User.UserStatus.ACTIVE)
                    .build();
            userRepository.save(residentUser1);

            BuildingOwner owner1 = ownerRepository.findByUser(ownerUser1).orElse(null);
            Building b1 = buildingRepository.findByOwner(owner1).stream().findFirst().orElse(null);
            if (b1 == null && owner1 != null) {
                b1 = Building.builder()
                        .owner(owner1)
                        .buildingName("Skyline Luxury Towers")
                        .buildingNumber("B-101")
                        .totalFlats(24)
                        .location("742 Evergreen Terrace, Sector 4")
                        .build();
                buildingRepository.save(b1);
            }

            if (b1 != null) {
                Resident r1 = Resident.builder()
                        .user(residentUser1)
                        .building(b1)
                        .name("Michael Scott")
                        .email("michael.resident@gmail.com")
                        .phone("+1 555-9012")
                        .apartmentNumber("Apt 4B")
                        .invitationToken("INV-TOKEN-001")
                        .invitationStatus(Resident.InvitationStatus.ACCEPTED)
                        .accountStatus(Resident.AccountStatus.ACTIVE)
                        .build();
                residentRepository.save(r1);

                WaterMeter m1 = WaterMeter.builder()
                        .building(b1)
                        .resident(r1)
                        .meterNumber("WM-SKY-4B")
                        .installationDate(LocalDate.now().minusMonths(6))
                        .status(WaterMeter.MeterStatus.ACTIVE)
                        .build();
                meterRepository.save(m1);

                WaterUsage u1 = WaterUsage.builder()
                        .meter(m1)
                        .month(7)
                        .year(2026)
                        .previousReading(120.0)
                        .currentReading(155.0)
                        .unitsConsumed(35.0)
                        .build();
                usageRepository.save(u1);

                Bill bill1 = Bill.builder()
                        .resident(r1)
                        .waterUsage(u1)
                        .billMonth(7)
                        .billYear(2026)
                        .units(35.0)
                        .amount(110.00)
                        .dueDate(LocalDate.now().plusDays(15))
                        .status(Bill.BillStatus.UNPAID)
                        .build();
                billRepository.save(bill1);

                Notification n1 = Notification.builder()
                        .resident(r1)
                        .title("New Water Bill Issued")
                        .message("Your water bill for July 2026 of $110.00 is now available.")
                        .isRead(false)
                        .build();
                notificationRepository.save(n1);
            }
        } else {
            residentUser1.setPassword(encodedPassword);
            residentUser1.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(residentUser1);
        }

        // Ensure sample notifications exist for resident
        Resident resMichael = residentRepository.findByUserEmail("michael.resident@gmail.com")
                .or(() -> residentRepository.findByEmail("michael.resident@gmail.com")).orElse(null);
        if (resMichael != null && notificationRepository.findByResidentResidentIdOrderByCreatedAtDesc(resMichael.getResidentId()).isEmpty()) {
            Notification n1 = Notification.builder()
                    .resident(resMichael)
                    .title("New Water Bill Issued")
                    .message("Your water bill for July 2026 of $110.00 is now available. Due date: Aug 15, 2026.")
                    .isRead(false)
                    .build();
            notificationRepository.save(n1);

            Notification n2 = Notification.builder()
                    .resident(resMichael)
                    .title("Water Conservation Alert")
                    .message("Summer water conservation rates apply for usage above 30 units.")
                    .isRead(false)
                    .build();
            notificationRepository.save(n2);
        }

        // 5. Ensure Tariff rules exist
        if (tariffRepository.count() == 0) {
            tariffRepository.save(new Tariff(null, 0, 20, 2.50, 10.00));
            tariffRepository.save(new Tariff(null, 21, 50, 4.00, 15.00));
            tariffRepository.save(new Tariff(null, 51, 1000, 6.50, 25.00));
        }

        // 6. Synchronize PostgreSQL sequence to avoid primary key duplicate errors
        try {
            org.hibernate.Session session = entityManager.unwrap(org.hibernate.Session.class);
            session.doWork(connection -> {
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                if ("PostgreSQL".equalsIgnoreCase(dbProduct)) {
                    try (java.sql.Statement stmt = connection.createStatement()) {
                        stmt.execute("SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1))");
                        stmt.execute("SELECT setval('building_owners_owner_id_seq', COALESCE((SELECT MAX(owner_id) FROM building_owners), 1))");
                        stmt.execute("SELECT setval('buildings_building_id_seq', COALESCE((SELECT MAX(building_id) FROM buildings), 1))");
                        stmt.execute("SELECT setval('residents_resident_id_seq', COALESCE((SELECT MAX(resident_id) FROM residents), 1))");
                        stmt.execute("SELECT setval('water_meters_meter_id_seq', COALESCE((SELECT MAX(meter_id) FROM water_meters), 1))");
                        stmt.execute("SELECT setval('water_usage_usage_id_seq', COALESCE((SELECT MAX(usage_id) FROM water_usage), 1))");
                        stmt.execute("SELECT setval('tariff_tariff_id_seq', COALESCE((SELECT MAX(tariff_id) FROM tariff), 1))");
                        stmt.execute("SELECT setval('bills_bill_id_seq', COALESCE((SELECT MAX(bill_id) FROM bills), 1))");
                        stmt.execute("SELECT setval('notifications_notification_id_seq', COALESCE((SELECT MAX(notification_id) FROM notifications), 1))");
                    } catch (Exception ex) {
                        log.debug("Error executing sequence sync: {}", ex.getMessage());
                    }
                    log.info("PostgreSQL Sequences synchronized successfully!");
                }
            });
        } catch (Exception e) {
            log.debug("Sequence synchronization skipped: {}", e.getMessage());
        }

        log.info("DataInitializer completed: All default user passwords updated for Admin@123456");
    }
}
