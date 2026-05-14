package com.travelnest.hotel.entity;

import com.travelnest.user.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class HotelEntity extends BaseAuditEntity {

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

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @Column(name = "cancel_policy_vi", columnDefinition = "TEXT")
    private String cancelPolicyVi;

    @Column(name = "cancel_policy_en", columnDefinition = "TEXT")
    private String cancelPolicyEn;

    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "star_rating", nullable = false)
    private Byte starRating;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    @OrderBy("basePrice asc")
    private Set<RoomTypeEntity> roomTypes = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_amenity_map",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @OrderBy("nameVi asc")
    private Set<HotelAmenityEntity> amenities = new LinkedHashSet<>();
}
