package com.travelnest.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @Column(name = "table_code", nullable = false, length = 20)
    private String tableCode;

    @Column(nullable = false)
    private Byte capacity;

    @Column(name = "zone_vi", length = 50)
    private String zoneVi;

    @Column(name = "zone_en", length = 50)
    private String zoneEn;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
