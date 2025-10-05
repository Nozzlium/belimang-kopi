package com.kopi.belimang.core.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    String username;

    @Column
    String email;

    @Column
    String password;

    @Column(nullable = false)
    boolean isAdmin;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    ZonedDateTime updatedAt;
}
