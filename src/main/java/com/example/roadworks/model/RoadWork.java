package com.example.roadworks.model;

import com.example.roadworks.model.enums.WorkStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String roadName;                // ex: "Autostrada A1", "DN1"

    private String roadCode;                // ex: "A1", "DN1", "DJ102"

    private String county;                  // ex: "Prahova", "Ilfov"

    private String sectionDescription;

    @NotNull
    private Integer startKm;

    @NotNull
    private Integer endKm;

    @NotNull
    private LocalDate repairStartDate;

    @NotNull
    private LocalDate repairEndDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkStatus status = WorkStatus.PLANNED;

    private String contractorName;

    @Column(length = 1000)
    private String notes;

    // Temporary traffic signs
    @Builder.Default
    private boolean temporarySignsPlaced = false;

    @Builder.Default
    private boolean temporarySignsRemoved = false;

    private LocalDate signsPlacedDate;

    private LocalDate signsRemovedDate;             // actual removal date

    private LocalDate signsExpectedRemovalDate;     // planned removal date

    // Audit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_id")
    private User lastModifiedBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastModifiedAt;

    /** Days left until expiry (negative if already expired) */
    public long getDaysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), repairEndDate);
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(repairEndDate);
    }

    /** True if signs were not removed on time */
    public boolean isSignsLate() {
        return temporarySignsPlaced && !temporarySignsRemoved
                && signsExpectedRemovalDate != null
                && LocalDate.now().isAfter(signsExpectedRemovalDate);
    }
}