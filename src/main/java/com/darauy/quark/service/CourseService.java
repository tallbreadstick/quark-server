package com.darauy.quark.service;

import com.darauy.quark.dto.CourseRequest;
import com.darauy.quark.entity.courses.*;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.entity.courses.lesson.Page;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CourseTagRepository courseTagRepository;

    @Autowired
    private CourseSharedRepository courseSharedRepository;

    @Autowired
    private UserRepository userRepository;

    // ---------------- CREATE COURSE ----------------
    @Transactional
    public Course createCourse(User owner, CourseRequest request) {
        validateCourseRequest(request);

        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .introduction(request.getIntroduction())
                .version(1)
                .owner(owner)
                .build();

        courseRepository.save(course);

        assignTags(course, request.getTags());

        return course;
    }

    // ---------------- FORK COURSE ----------------
    @Transactional
    public Course forkCourse(User owner, Integer courseId, CourseRequest request) {
        Course origin = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        if (origin.getOrigin() != null) {
            origin = origin.getOrigin(); // always fork original
        }

        Course forked = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .introduction(request.getIntroduction())
                .version(1)
                .origin(origin)
                .owner(owner)
                .build();

        courseRepository.save(forked);

        // Copy tags
        Set<CourseTag> originalTags = courseTagRepository.findByCourse(origin);
        originalTags.forEach(ct -> courseTagRepository.save(
                CourseTag.builder()
                        .course(forked)
                        .tag(ct.getTag())
                        .build()
        ));

        // Deep copy chapters, lessons, activities, sections, pages
        deepCopyCourseStructure(origin, forked);

        return forked;
    }

    // ---------------- EDIT COURSE ----------------
    @Transactional
    public Course editCourse(User owner, Integer courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        if (!course.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("User does not own this course");
        }

        validateCourseRequest(request);

        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setIntroduction(request.getIntroduction());
        courseRepository.save(course);

        courseTagRepository.deleteByCourse(course);
        assignTags(course, request.getTags());

        return course;
    }

    // ---------------- DELETE COURSE ----------------
    @Transactional
    public void deleteCourse(User owner, Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        if (!course.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("User does not own this course");
        }

        courseRepository.delete(course);
    }

    // ---------------- FETCH COURSES BY FILTER ----------------
    public List<Course> fetchCoursesByFilter(User user,
                                             Boolean myCourses,
                                             Boolean sharedWithMe,
                                             Boolean forkable,
                                             List<String> tags,
                                             String sortBy,
                                             String order,
                                             String search) {

        List<Course> allCourses = courseRepository.findAll();

        return allCourses.stream()
                .filter(c -> {
                    if (myCourses != null && myCourses) {
                        return c.getOwner().getId().equals(user.getId());
                    }
                    return true;
                })
                .filter(c -> {
                    if (sharedWithMe != null && sharedWithMe) {
                        return courseSharedRepository.findByUserAndCourse(user, c).isPresent();
                    }
                    return true;
                })
                .filter(c -> {
                    if (forkable != null) {
                        return forkable.equals(Boolean.TRUE) ? true : true; // TODO: add forkable field
                    }
                    return true;
                })
                .filter(c -> {
                    if (tags != null && !tags.isEmpty()) {
                        Set<String> courseTagNames = courseTagRepository.findByCourse(c)
                                .stream().map(ct -> ct.getTag().getName()).collect(Collectors.toSet());
                        return courseTagNames.containsAll(tags);
                    }
                    return true;
                })
                .filter(c -> {
                    if (StringUtils.hasText(search)) {
                        return c.getName().toLowerCase().contains(search.toLowerCase()) ||
                                (c.getDescription() != null && c.getDescription().toLowerCase().contains(search.toLowerCase()));
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    if ("name".equals(sortBy)) {
                        return "descending".equals(order) ?
                                b.getName().compareTo(a.getName()) : a.getName().compareTo(b.getName());
                    } else if ("date_created".equals(sortBy)) {
                        return "descending".equals(order) ?
                                b.getCreatedAt().compareTo(a.getCreatedAt()) : a.getCreatedAt().compareTo(b.getCreatedAt());
                    }
                    return 0;
                })
                .toList();
    }

    // ---------------- FETCH COURSE WITH CHAPTERS ----------------
    public Course fetchCourseWithChapters(User user, Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        if (!course.getOwner().getId().equals(user.getId()) &&
                courseSharedRepository.findByUserAndCourse(user, course).isEmpty()) {
            throw new SecurityException("User cannot access this course");
        }

        // --- Eager load chapters and children in batch ---
        List<Chapter> chapters = chapterRepository.findByCourse(course);

        List<Lesson> lessons = lessonRepository.findByChapterIn(chapters);
        List<Activity> activities = activityRepository.findByChapterIn(chapters);

        List<Page> pages = pageRepository.findByLessonIn(lessons);
        List<Section> sections = sectionRepository.findByActivityIn(activities);

        // Optional: you can attach these lists to the entities if needed
        // for example, if you want the course object to carry its children fully loaded

        return course;
    }

    // ---------------- SHARE COURSE ----------------
    @Transactional
    public void shareCourse(User owner, Integer courseId, Integer targetUserId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        if (!course.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("User does not own this course");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NoSuchElementException("Target user not found"));

        if (courseSharedRepository.findByUserAndCourse(targetUser, course).isEmpty()) {
            courseSharedRepository.save(CourseShared.builder()
                    .user(targetUser)
                    .course(course)
                    .build());
        }
    }

    // ---------------- HELPER METHODS ----------------
    private void validateCourseRequest(CourseRequest request) {
        if (request.getName() == null || request.getName().length() < 10 || request.getName().length() > 255) {
            throw new IllegalArgumentException("Course name must be between 10 and 255 characters");
        }

        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("Course description cannot exceed 255 characters");
        }

        if (request.getTags() != null && request.getTags().size() > 3) {
            throw new IllegalArgumentException("Cannot assign more than 3 tags");
        }
    }

    private void assignTags(Course course, List<String> tagNames) {
        if (tagNames == null) return;

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseThrow(() -> new NoSuchElementException("Tag not found: " + tagName));
            courseTagRepository.save(CourseTag.builder()
                    .course(course)
                    .tag(tag)
                    .build());
        }
    }

    @Transactional
    public void deepCopyCourseStructure(Course origin, Course forked) {
        List<Chapter> chapters = chapterRepository.findByCourse(origin);

        for (Chapter ch : chapters) {
            Chapter newChapter = Chapter.builder()
                    .name(ch.getName())
                    .idx(ch.getIdx())
                    .description(ch.getDescription())
                    .icon(ch.getIcon())
                    .course(forked)
                    .build();
            chapterRepository.save(newChapter);

            // Copy Lessons
            List<Lesson> lessons = lessonRepository.findByChapter(ch);
            for (Lesson l : lessons) {
                Lesson newLesson = Lesson.builder()
                        .name(l.getName())
                        .idx(l.getIdx())
                        .description(l.getDescription())
                        .icon(l.getIcon())
                        .finishMessage(l.getFinishMessage())
                        .version(l.getVersion())
                        .chapter(newChapter)
                        .build();
                lessonRepository.save(newLesson);

                // Copy Pages
                List<Page> pages = pageRepository.findByLesson(l);
                for (Page p : pages) {
                    Page newPage = Page.builder()
                            .idx(p.getIdx())
                            .content(p.getContent())
                            .lesson(newLesson)
                            .build();
                    pageRepository.save(newPage);
                }
            }

            // Copy Activities
            List<Activity> activities = activityRepository.findByChapter(ch);
            for (Activity a : activities) {
                Activity newActivity = Activity.builder()
                        .name(a.getName())
                        .idx(a.getIdx())
                        .description(a.getDescription())
                        .icon(a.getIcon())
                        .finishMessage(a.getFinishMessage())
                        .ruleset(a.getRuleset())
                        .version(a.getVersion())
                        .chapter(newChapter)
                        .build();
                activityRepository.save(newActivity);

                // Copy Sections
                List<Section> sections = sectionRepository.findByActivity(a);
                for (Section s : sections) {
                    Section newSection = Section.builder()
                            .idx(s.getIdx())
                            .content(s.getContent())
                            .activity(newActivity)
                            .build();
                    sectionRepository.save(newSection);
                }
            }
        }
    }
}
