package com.example.roadworks.service;

import com.example.roadworks.model.RoadWork;
import com.example.roadworks.model.User;
import com.example.roadworks.model.enums.WorkStatus;
import com.example.roadworks.repository.AlertRepository;
import com.example.roadworks.repository.RoadWorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoadWorkService {

    private final RoadWorkRepository roadWorkRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<RoadWork> findAll() {
        return roadWorkRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RoadWork> findById(Long id) {
        return roadWorkRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<RoadWork> findByStatus(WorkStatus status) {
        return roadWorkRepository.findByStatusOrderByRepairEndDateAsc(status);
    }

    @Transactional(readOnly = true)
    public List<RoadWork> findActive() {
        return roadWorkRepository.findByStatusInOrderByRepairEndDateAsc(
                List.of(WorkStatus.IN_PROGRESS, WorkStatus.EXTENDED));
    }

    @Transactional(readOnly = true)
    public List<RoadWork> findExpiringInDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        return roadWorkRepository.findExpiringBetween(
                List.of(WorkStatus.IN_PROGRESS, WorkStatus.EXTENDED), today, threshold);
    }

    @Transactional(readOnly = true)
    public List<RoadWork> findWithLateSignRemoval() {
        return roadWorkRepository.findWithLateSignRemoval(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long total = roadWorkRepository.count();
        long inProgress = roadWorkRepository.countByStatus(WorkStatus.IN_PROGRESS)
                         + roadWorkRepository.countByStatus(WorkStatus.EXTENDED);
        long planned = roadWorkRepository.countByStatus(WorkStatus.PLANNED);
        long completed = roadWorkRepository.countByStatus(WorkStatus.COMPLETED);
        long expiring7 = findExpiringInDays(7).size();
        long lateSign = findWithLateSignRemoval().size();
        long unreadAlerts = alertRepository.countByReadFalse();
        return new DashboardStats(total, inProgress, planned, completed, expiring7, lateSign, unreadAlerts);
    }

    public RoadWork save(RoadWork roadWork, User currentUser) {
        roadWork.setLastModifiedAt(LocalDateTime.now());
        roadWork.setLastModifiedBy(currentUser);
        if (roadWork.getId() == null) {
            roadWork.setCreatedAt(LocalDateTime.now());
            roadWork.setCreatedBy(currentUser);
        }
        return roadWorkRepository.save(roadWork);
    }

    /** Extend repair period */
    public RoadWork extend(Long id, LocalDate newEndDate, String reason, User currentUser) {
        RoadWork rw = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work with id=" + id + " does not exist."));
        rw.setRepairEndDate(newEndDate);
        rw.setStatus(WorkStatus.EXTENDED);
        String existingNotes = rw.getNotes() == null ? "" : rw.getNotes();
        rw.setNotes(existingNotes + "\n[EXTENDED " + LocalDate.now() + "] " + reason);
        return save(rw, currentUser);
    }

    /** Mark work as completed */
    public RoadWork complete(Long id, User currentUser) {
        RoadWork rw = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work with id=" + id + " does not exist."));
        rw.setStatus(WorkStatus.COMPLETED);
        return save(rw, currentUser);
    }

    /** Mark signs as removed */
    public RoadWork markSignsRemoved(Long id, User currentUser) {
        RoadWork rw = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work with id=" + id + " does not exist."));
        rw.setTemporarySignsRemoved(true);
        rw.setSignsRemovedDate(LocalDate.now());
        return save(rw, currentUser);
    }

    /** Mark signs as placed */
    public RoadWork markSignsPlaced(Long id, LocalDate expectedRemovalDate, User currentUser) {
        RoadWork rw = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work with id=" + id + " does not exist."));
        rw.setTemporarySignsPlaced(true);
        rw.setTemporarySignsRemoved(false);
        rw.setSignsPlacedDate(LocalDate.now());
        rw.setSignsExpectedRemovalDate(expectedRemovalDate);
        return save(rw, currentUser);
    }

    public void delete(Long id) {
        RoadWork rw = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Road work with id=" + id + " does not exist."));
        alertRepository.deleteByRoadWork(rw);
        roadWorkRepository.delete(rw);
    }

    /** Stats record for dashboard */
    public record DashboardStats(long total, long inProgress, long planned,
                                 long completed, long expiring7Days,
                                 long lateSignRemoval, long unreadAlerts) {}
}
