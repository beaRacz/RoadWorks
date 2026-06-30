package com.example.roadworks.service;

import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.enums.AlertType;
import com.example.roadworks.model.enums.WorkStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Runs daily and generates automatic alerts for:
 * - Works expiring soon (3 and 7 days)
 * - Expired works not marked as completed
 * - Temporary signs not removed on time
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledAlertService {

    private final RoadWorkService roadWorkService;
    private final AlertService alertService;

    @Value("${roadworks.alert.warning.days:7}")
    private int warningDays;

    @Value("${roadworks.alert.critical.days:3}")
    private int criticalDays;

    @Scheduled(cron = "${roadworks.alert.schedule.cron:0 0 8 * * *}")
    public void runDailyAlertCheck() {
        log.info("=== Daily road works alert check ===");
        checkExpiringWarning();
        checkExpiringCritical();
        checkExpired();
        checkLateSignRemoval();
        log.info("=== Check completed ===");
    }

    /** Alerts for works expiring in 7 days */
    private void checkExpiringWarning() {
        List<RoadWork> expiring = roadWorkService.findExpiringInDays(warningDays);
        for (RoadWork rw : expiring) {
            long days = rw.getDaysUntilExpiry();
            if (days > criticalDays) { // avoid duplicating critical alerts
                String msg = String.format(
                        "Work on %s (km %d-%d) expires in %d days (date: %s). Do you want to extend it?",
                        rw.getRoadName(), rw.getStartKm(), rw.getEndKm(), days, rw.getRepairEndDate());
                alertService.createAlertIfNotExists(rw, AlertType.EXPIRING_7_DAYS, msg);
            }
        }
    }

    /** Critical alerts for works expiring in 3 days */
    private void checkExpiringCritical() {
        List<RoadWork> expiring = roadWorkService.findExpiringInDays(criticalDays);
        for (RoadWork rw : expiring) {
            long days = rw.getDaysUntilExpiry();
            String msg = String.format(
                    "WARNING! Work on %s (km %d-%d) expires in %d days (date: %s). Action required!",
                    rw.getRoadName(), rw.getStartKm(), rw.getEndKm(), days, rw.getRepairEndDate());
            alertService.createAlertIfNotExists(rw, AlertType.EXPIRING_3_DAYS, msg);
        }
    }

    /** Alerts for works whose period ended without completion */
    private void checkExpired() {
        List<RoadWork> expired = roadWorkService.findByStatus(WorkStatus.IN_PROGRESS)
                .stream()
                .filter(RoadWork::isExpired)
                .toList();
        for (RoadWork rw : expired) {
            String msg = String.format(
                    "Work on %s (km %d-%d) exceeded the end date (%s) without being marked as complete!",
                    rw.getRoadName(), rw.getStartKm(), rw.getEndKm(), rw.getRepairEndDate());
            alertService.createAlertIfNotExists(rw, AlertType.EXPIRED, msg);
        }
    }

    /** Alerts for temporary signs not removed on time */
    private void checkLateSignRemoval() {
        List<RoadWork> lateSignWork = roadWorkService.findWithLateSignRemoval();
        for (RoadWork rw : lateSignWork) {
            String msg = String.format(
                    "Temporary signs for work on %s (km %d-%d) were not removed! Deadline was: %s.",
                    rw.getRoadName(), rw.getStartKm(), rw.getEndKm(), rw.getSignsExpectedRemovalDate());
            alertService.createAlertIfNotExists(rw, AlertType.SIGNS_NOT_REMOVED, msg);
        }
    }
}
