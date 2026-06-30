package com.example.roadworks.service;

import com.example.roadworks.model.Alert;
import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.enums.AlertType;
import com.example.roadworks.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<Alert> findAllUnread() {
        return alertRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Alert> findAll() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return alertRepository.countByReadFalse();
    }

    @Transactional(readOnly = true)
    public List<Alert> findByRoadWork(RoadWork rw) {
        return alertRepository.findByRoadWorkOrderByCreatedAtDesc(rw);
    }

    public void markAsRead(Long id) {
        alertRepository.findById(id).ifPresent(alert -> {
            alert.setRead(true);
            alertRepository.save(alert);
        });
    }

    public void markAllAsRead() {
        List<Alert> unread = alertRepository.findByReadFalseOrderByCreatedAtDesc();
        unread.forEach(a -> a.setRead(true));
        alertRepository.saveAll(unread);
    }

    /**
     * Creates an alert for a road work, avoiding duplicates on the same day.
     */
    public void createAlertIfNotExists(RoadWork roadWork, AlertType alertType, String message) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        Optional<Alert> existing = alertRepository.findByRoadWorkAndAlertTypeAndCreatedAtAfter(
                roadWork, alertType, startOfDay);
        if (existing.isEmpty()) {
            Alert alert = Alert.builder()
                    .roadWork(roadWork)
                    .alertType(alertType)
                    .message(message)
                    .read(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            alertRepository.save(alert);
            log.info("Alert created: [{}] for road work id={} - {}", alertType, roadWork.getId(), message);
        }
    }
}
