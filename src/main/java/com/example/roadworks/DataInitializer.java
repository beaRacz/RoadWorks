package com.example.roadworks;

import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.User;
import com.example.roadworks.model.enums.UserRole;
import com.example.roadworks.model.enums.WorkStatus;
import com.example.roadworks.repository.RoadWorkRepository;
import com.example.roadworks.repository.UserRepository;
import com.example.roadworks.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoadWorkRepository roadWorkRepository;

    @Override
    public void run(String... args) {
        seedUsers();
        seedRoadWorks();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            userService.createUser("admin", "admin123", "Administrator Principal", "admin@roadworks.ro", UserRole.ADMIN);
            userService.createUser("ionescu", "password123", "Ion Ionescu", "ionescu@roadworks.ro", UserRole.SUPERVISOR);
            userService.createUser("popescu", "password123", "Maria Popescu", "popescu@roadworks.ro", UserRole.SUPERVISOR);
            userService.createUser("viewer", "viewer123", "Demo Viewer", "viewer@roadworks.ro", UserRole.VIEWER);
            log.info("Demo users created: admin / admin123, ionescu / password123, viewer / viewer123");
        }
    }

    private void seedRoadWorks() {
        if (roadWorkRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow();
            LocalDate today = LocalDate.now();

            roadWorkRepository.save(RoadWork.builder()
                    .roadName("Autostrada A1").roadCode("A1").county("Prahova")
                    .sectionDescription("Carriageway and median barrier repair")
                    .startKm(45).endKm(52)
                    .repairStartDate(today.minusDays(30))
                    .repairEndDate(today.plusDays(5))          // expires in 5 days - 7-day alert
                    .status(WorkStatus.IN_PROGRESS)
                    .contractorName("SC Drumuri Moderne SRL")
                    .temporarySignsPlaced(true).signsPlacedDate(today.minusDays(30))
                    .signsExpectedRemovalDate(today.plusDays(5))
                    .createdBy(admin).createdAt(LocalDateTime.now().minusDays(30))
                    .build());

            roadWorkRepository.save(RoadWork.builder()
                    .roadName("Autostrada A1").roadCode("A1").county("Ilfov")
                    .sectionDescription("Outer lanes rehabilitation")
                    .startKm(10).endKm(18)
                    .repairStartDate(today.minusDays(60))
                    .repairEndDate(today.plusDays(2))          // expires in 2 days - critical alert
                    .status(WorkStatus.IN_PROGRESS)
                    .contractorName("SC Asfalt Pro SA")
                    .temporarySignsPlaced(true).signsPlacedDate(today.minusDays(60))
                    .signsExpectedRemovalDate(today.plusDays(2))
                    .createdBy(admin).createdAt(LocalDateTime.now().minusDays(60))
                    .build());

            roadWorkRepository.save(RoadWork.builder()
                    .roadName("DN1").roadCode("DN1").county("Brasov")
                    .sectionDescription("Slope consolidation and lane marking restoration")
                    .startKm(120).endKm(130)
                    .repairStartDate(today.minusDays(90))
                    .repairEndDate(today.minusDays(5))         // EXPIRED - signs not removed!
                    .status(WorkStatus.IN_PROGRESS)
                    .contractorName("SC ConstrucRoutes SRL")
                    .temporarySignsPlaced(true).signsPlacedDate(today.minusDays(90))
                    .signsExpectedRemovalDate(today.minusDays(5))
                    .temporarySignsRemoved(false)
                    .createdBy(admin).createdAt(LocalDateTime.now().minusDays(90))
                    .build());

            roadWorkRepository.save(RoadWork.builder()
                    .roadName("DN7").roadCode("DN7").county("Valcea")
                    .sectionDescription("Pothole patching and wearing course restoration")
                    .startKm(210).endKm(215)
                    .repairStartDate(today.minusDays(45))
                    .repairEndDate(today.minusDays(10))
                    .status(WorkStatus.COMPLETED)
                    .contractorName("SC ViaRoum SA")
                    .temporarySignsPlaced(true).signsPlacedDate(today.minusDays(45))
                    .signsExpectedRemovalDate(today.minusDays(10))
                    .temporarySignsRemoved(true).signsRemovedDate(today.minusDays(9))
                    .createdBy(admin).createdAt(LocalDateTime.now().minusDays(45))
                    .notes("Work completed on schedule. Signs removed the day after completion.")
                    .build());

            roadWorkRepository.save(RoadWork.builder()
                    .roadName("A3").roadCode("A3").county("Cluj")
                    .sectionDescription("New construction – Turda-Gilau section")
                    .startKm(300).endKm(340)
                    .repairStartDate(today.minusDays(15))
                    .repairEndDate(today.plusDays(180))
                    .status(WorkStatus.IN_PROGRESS)
                    .contractorName("SC Transylvanian Highways SRL")
                    .temporarySignsPlaced(true).signsPlacedDate(today.minusDays(15))
                    .signsExpectedRemovalDate(today.plusDays(180))
                    .createdBy(admin).createdAt(LocalDateTime.now().minusDays(15))
                    .build());

            roadWorkRepository.save(RoadWork.builder()
                    .roadName("DN2").roadCode("DN2").county("Buzau")
                    .sectionDescription("Emergency repairs after flooding")
                    .startKm(88).endKm(90)
                    .repairStartDate(today.plusDays(3))
                    .repairEndDate(today.plusDays(30))
                    .status(WorkStatus.PLANNED)
                    .contractorName("SC Road Emergency SA")
                    .createdBy(admin).createdAt(LocalDateTime.now())
                    .build());

            log.info("Demo road work data created (6 records).");
        }
    }
}
