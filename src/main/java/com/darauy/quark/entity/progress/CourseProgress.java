package com.darauy.quark.entity.progress;

import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "course_progress",
        indexes = {
                @Index(name = "idx_course_progress_user", columnList = "user_id"),
                @Index(name = "idx_course_progress_course", columnList = "course_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgress {

    @EmbeddedId
    private CourseProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Boolean enrolled;

    public CourseProgress(User user, Course course) {
        this.user = user;
        this.course = course;
        this.id = new CourseProgressId(user.getId(), course.getId());
        this.enrolled = true;
    }
}
