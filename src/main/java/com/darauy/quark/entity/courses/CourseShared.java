package com.darauy.quark.entity.courses;

import com.darauy.quark.entity.users.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_shared")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseShared {

    @EmbeddedId
    private CourseSharedId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
