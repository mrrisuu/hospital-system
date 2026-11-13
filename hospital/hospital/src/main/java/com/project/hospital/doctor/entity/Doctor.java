package com.project.hospital.doctor.entity;

import com.project.hospital.poli.entity.Poli;
import com.project.hospital.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poli_id", nullable = false)
    private Poli poli;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}