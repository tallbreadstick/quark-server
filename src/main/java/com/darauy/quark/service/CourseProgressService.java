package com.darauy.quark.service;

import com.darauy.quark.entity.progress.CourseProgress;
import com.darauy.quark.entity.progress.CourseProgressId;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.repository.CourseProgressRepository;
import com.darauy.quark.repository.UserRepository;
import com.darauy.quark.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    /**
     * Enroll user in a course (create course progress entry)
     * @param userId the user id
     * @param courseId the course id
     * @return the created CourseProgress
     */
    @Transactional
    public CourseProgress enrollUserInCourse(Integer userId, Integer courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        // Check if already enrolled
        if (courseProgressRepository.findByUserAndCourse(user, course).isPresent()) {
            throw new IllegalArgumentException("User is already enrolled in this course");
        }

        CourseProgress progress = CourseProgress.builder()
                .user(user)
                .course(course)
                .build();

        return courseProgressRepository.save(progress);
    }

    /**
     * Unenroll user from a course
     * @param userId the user id
     * @param courseId the course id
     */
    @Transactional
    public void unenrollUserFromCourse(Integer userId, Integer courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        CourseProgress progress = courseProgressRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new NoSuchElementException("Course progress not found"));

        courseProgressRepository.delete(progress);
    }

    /**
     * Get all enrolled courses for a user
     * @param userId the user id
     * @return list of CourseProgress entries
     */
    public List<CourseProgress> getEnrolledCourses(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return courseProgressRepository.findByUserAndEnrolledTrue(user);
    }

    /**
     * Get course progress for a specific user and course
     * @param userId the user id
     * @param courseId the course id
     * @return the CourseProgress
     */
    public CourseProgress getCourseProgress(Integer userId, Integer courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        return courseProgressRepository.findByUserAndCourse(user, course)
                .orElseThrow(() -> new NoSuchElementException("Course progress not found"));
    }

    /**
     * Check if user is enrolled in a course
     * @param userId the user id
     * @param courseId the course id
     * @return true if enrolled, false otherwise
     */
    public Boolean isEnrolled(Integer userId, Integer courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        return courseProgressRepository.findByUserAndCourse(user, course)
                .map(CourseProgress::getEnrolled)
                .orElse(false);
    }
}
