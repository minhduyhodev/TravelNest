package com.travelnest.tour.entity;

import com.travelnest.user.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tours")
public class TourEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_vi", nullable = false, length = 255)
    private String nameVi;

    @Column(name = "name_en", nullable = false, length = 255)
    private String nameEn;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Column(name = "description_vi", columnDefinition = "TEXT")
    private String descriptionVi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "duration_days", nullable = false)
    private Byte durationDays;

    @Column(name = "duration_nights", nullable = false)
    private Byte durationNights;

    @Column(name = "destination_vi", nullable = false, length = 150)
    private String destinationVi;

    @Column(name = "destination_en", nullable = false, length = 150)
    private String destinationEn;

    @Column(name = "departure_point_vi", length = 255)
    private String departurePointVi;

    @Column(name = "departure_point_en", length = 255)
    private String departurePointEn;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    @Column(name = "min_guests", nullable = false)
    private Integer minGuests;

    @Column(name = "includes_vi", columnDefinition = "TEXT")
    private String includesVi;

    @Column(name = "includes_en", columnDefinition = "TEXT")
    private String includesEn;

    @Column(name = "excludes_vi", columnDefinition = "TEXT")
    private String excludesVi;

    @Column(name = "excludes_en", columnDefinition = "TEXT")
    private String excludesEn;

    @Column(name = "requirements_vi", columnDefinition = "TEXT")
    private String requirementsVi;

    @Column(name = "requirements_en", columnDefinition = "TEXT")
    private String requirementsEn;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    @OrderBy("dayNumber asc")
    private Set<TourItineraryEntity> itineraries = new LinkedHashSet<>();

    @OneToMany(mappedBy = "tour", fetch = FetchType.LAZY)
    @OrderBy("startDate asc")
    private Set<TourSlotEntity> slots = new LinkedHashSet<>();
}
