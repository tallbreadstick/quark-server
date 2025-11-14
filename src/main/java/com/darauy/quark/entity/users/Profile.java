package com.darauy.quark.entity.users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne
    @MapsId // Shares PK with User
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image; // nullable

    @Column(length = 255)
    private String bio; // nullable

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
