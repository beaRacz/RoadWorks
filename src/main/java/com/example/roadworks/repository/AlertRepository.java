package com.example.roadworks.repository;

import com.example.roadworks.model.Alert;
import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByReadFalseOrderByCreatedAtDesc();

    List<Alert> findAllByOrderByCreatedAtDesc();

    long countByReadFalse();

    /** Avoid duplicates: check if an alert of the same type for the same road work was already created today */
    Optional<Alert> findByRoadWorkAndAlertTypeAndCreatedAtAfter(
            RoadWork roadWork,
            AlertType alertType,
            LocalDateTime since);

    List<Alert> findByRoadWorkOrderByCreatedAtDesc(RoadWork roadWork);

    void deleteByRoadWork(RoadWork roadWork);
}
