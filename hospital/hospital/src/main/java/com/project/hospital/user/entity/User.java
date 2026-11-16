package com.project.hospital.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

/*
CATATAN

Kolom-kolomnya sengaja cocok dengan V1__create_users.sql. @Enumerated(EnumType.STRING) menyimpan
nama peran (ADMIN), bukan angka, sesuai CHECK di tabel. @PrePersist mengisi createdAt saat pertama
disimpan.
 */
