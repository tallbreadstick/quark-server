package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.CourseShared;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSharedRepository extends JpaRepository<CourseShared, Integer> {
    List<CourseShared> findByUser(User user);
    Optional<CourseShared> findByUserAndCourse(User user, Course course);
    void deleteByCourse(Course course);
}
