package com.darauy.quark.entity.achievements;

import com.darauy.quark.entity.users.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.darauy.quark.entity.achievements.Certificate;

@Entity
@Table(name = "user_certificates")
public class UserCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    // Constructors
    public UserCertificate() {
        this.earnedAt = LocalDateTime.now();
    }

    public UserCertificate(User user, Certificate certificate) {
        this.user = user;
        this.certificate = certificate;
        this.earnedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.earnedAt = LocalDateTime.now();
    }
}