package com.example.roadworks.repository;

import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.enums.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoadWorkRepository extends JpaRepository<RoadWork, Long> {

    List<RoadWork> findByStatusOrderByRepairEndDateAsc(WorkStatus status);

    List<RoadWork> findByStatusInOrderByRepairEndDateAsc(List<WorkStatus> statuses);

    List<RoadWork> findByCountyIgnoreCaseOrderByRepairEndDateAsc(String county);

    List<RoadWork> findByRoadCodeIgnoreCaseOrderByRepairEndDateAsc(String roadCode);

        /** Active works whose end date is <= threshold date (for expiry alerts) */
    @Query("SELECT r FROM RoadWork r WHERE r.status IN :statuses AND r.repairEndDate <= :threshold AND r.repairEndDate >= :today")
    List<RoadWork> findExpiringBetween(
            @Param("statuses") List<WorkStatus> statuses,
            @Param("today") LocalDate today,
            @Param("threshold") LocalDate threshold);

        /** Expired works (end date < today) that are not COMPLETED/CANCELLED */
    @Query("SELECT r FROM RoadWork r WHERE r.status NOT IN :excludedStatuses AND r.repairEndDate < :today")
    List<RoadWork> findExpired(
            @Param("excludedStatuses") List<WorkStatus> excludedStatuses,
            @Param("today") LocalDate today);

        /** Works with temporary signs placed, not removed, and expected removal date already passed */
    @Query("SELECT r FROM RoadWork r WHERE r.temporarySignsPlaced = true AND r.temporarySignsRemoved = false " +
           "AND r.signsExpectedRemovalDate IS NOT NULL AND r.signsExpectedRemovalDate < :today")
    List<RoadWork> findWithLateSignRemoval(@Param("today") LocalDate today);

    long countByStatus(WorkStatus status);
}
