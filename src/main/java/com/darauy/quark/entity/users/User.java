package com.darauy.quark.entity.users;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, nullable = false, unique = true)
    private String username;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 128, nullable = false)
    private String password;

    @Column(name = "user_type", nullable = false, length = 16)
    private String userType; // Should be "Educator" or "Learner"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // --- Relation: One user → one Profile ---
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Profile profile;
}
