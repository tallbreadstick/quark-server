package com.darauy.quark.service;

import com.darauy.quark.entity.courses.Course;
import com.darauy.quark.entity.courses.CourseTag;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.CourseRepository;
import com.darauy.quark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    // ----------- CREATE -----------
    @Transactional
    public Course createCourse(Course course) {
        if (course.getName() == null || course.getOwner() == null || course.getVersion() == null) {
            throw new IllegalArgumentException("Name, Owner, and Version are required");
        }

        // Ensure owner exists
        Optional<User> owner = userRepository.findById(course.getOwner().getId());
        if (owner.isEmpty()) throw new IllegalArgumentException("Owner not found");

        course.setOwner(owner.get());
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        // Handle derivedCourses and tags if null
        if (course.getTags() != null) {
            course.getTags().forEach(tag -> tag.setCourse(course));
        }

        return courseRepository.save(course);
    }

    // ----------- GET BY ID -----------
    public Course getCourseById(Integer id) {
        return courseRepository.findById(id).orElse(null);
    }

    // ----------- GET ALL -----------
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // ----------- PATCH (UPDATE) -----------
    @Transactional
    public Course updateCourse(Integer id, Map<String, Object> updates) {
        Optional<Course> optional = courseRepository.findById(id);
        if (optional.isEmpty()) return null;

        Course course = optional.get();

        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    course.setName((String) value);
                    break;
                case "description":
                    course.setDescription((String) value);
                    break;
                case "introduction":
                    course.setIntroduction((String) value);
                    break;
                case "version":
                    course.setVersion((Integer) value);
                    break;
                case "origin":
                    if (value != null) {
                        Integer originId = (Integer) value;
                        courseRepository.findById(originId).ifPresent(course::setOrigin);
                    } else {
                        course.setOrigin(null);
                    }
                    break;
                case "owner":
                    if (value != null) {
                        Integer ownerId = (Integer) value;
                        userRepository.findById(ownerId).ifPresent(course::setOwner);
                    }
                    break;
                default:
                    // ignore unknown fields
                    break;
            }
        });

        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    // ----------- DELETE -----------
    @Transactional
    public boolean deleteCourse(Integer id) {
        Optional<Course> optional = courseRepository.findById(id);
        if (optional.isEmpty()) return false;

        courseRepository.delete(optional.get());
        return true;
    }
}
