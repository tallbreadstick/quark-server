package com.darauy.quark.entity.progress;

import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "activity_progress",
        indexes = {
                @Index(name = "idx_activity_progress_user", columnList = "user_id"),
                @Index(name = "idx_activity_progress_activity", columnList = "activity_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityProgress {

    @EmbeddedId
    private ActivityProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("activityId")
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private Integer completedSections;

    public ActivityProgress(User user, Activity activity) {
        this.user = user;
        this.activity = activity;
        this.id = new ActivityProgressId(user.getId(), activity.getId());
        this.completedSections = 0;
    }
}
