package com.darauy.quark.entity.achievements;

import com.darauy.quark.entity.users.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.darauy.quark.entity.achievements.Badge;

@Entity
@Table(name = "user_badges")
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    // Constructors
    public UserBadge() {
        this.earnedAt = LocalDateTime.now();
    }

    public UserBadge(User user, Badge badge) {
        this.user = user;
        this.badge = badge;
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

    public Badge getBadge() {
        return badge;
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
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