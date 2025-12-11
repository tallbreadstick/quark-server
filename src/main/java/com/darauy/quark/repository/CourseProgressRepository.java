package com.darauy.quark.repository;

import com.darauy.quark.entity.progress.CourseProgress;
import com.darauy.quark.entity.progress.CourseProgressId;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.entity.courses.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, CourseProgressId> {
    List<CourseProgress> findByUser(User user);
    List<CourseProgress> findByCourse(Course course);
    Optional<CourseProgress> findByUserAndCourse(User user, Course course);
    List<CourseProgress> findByUserAndEnrolledTrue(User user);
}
