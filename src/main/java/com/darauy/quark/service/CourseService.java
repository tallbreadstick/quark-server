package com.darauy.quark.service;

import com.darauy.quark.dto.response.CourseContentResponse;
import com.darauy.quark.dto.response.CourseFilterResponse;
import com.darauy.quark.dto.request.CourseRequest;
import com.darauy.quark.entity.courses.*;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.entity.courses.lesson.Page;
import com.darauy.quark.entity.progress.CourseProgress;
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
    private CourseProgressRepository courseProgressRepository;

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
                .forkable(request.getForkable())
                .visibility(request.getVisibility())
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

        boolean isOwner = origin.getOwner().getId().equals(owner.getId());
        boolean isShared =
                courseSharedRepository.findByUserAndCourse(owner, origin).isPresent();

        // If you add a "forkable" boolean on Course, check that here:
        boolean isPublicForkable = origin.getForkable() != null && origin.getForkable();

        if (!isOwner && !isShared && !isPublicForkable) {
            throw new SecurityException("You are not allowed to fork this course.");
        }

        Course forked = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .introduction(request.getIntroduction())
                .forkable(request.getForkable())
                .visibility(request.getVisibility())
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
        course.setForkable(request.getForkable());
        course.setVisibility(request.getVisibility());
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

        courseTagRepository.deleteByCourse(course);
        courseSharedRepository.deleteByCourse(course);
        courseRepository.delete(course);
    }

    // ---------------- FETCH COURSES BY FILTER ----------------
    public List<CourseFilterResponse> fetchCoursesByFilter(
            User user,
            Boolean myCourses,
            Boolean sharedWithMe,
            Boolean enrolled,
            Boolean forkable,
            List<String> tags,
            String sortBy,
            String order,
            String search
    ) {

        // 1. Fetch all courses with relations in ONE query
        List<Course> allCourses = courseRepository.fetchCoursesWithRelations();

        // 2. Fetch all shares for the user in ONE query
        List<CourseShared> userShares = courseSharedRepository.findByUser(user);

        // 3. Convert to fast-lookup set
        Set<Integer> sharedCourseIds = userShares.stream()
                .map(cs -> cs.getCourse().getId())
                .collect(Collectors.toSet());

        // 4. Fetch enrolled courses for the user
        List<CourseProgress> enrolledProgress = courseProgressRepository.findByUserAndEnrolledTrue(user);
        Set<Integer> enrolledCourseIds = enrolledProgress.stream()
                .map(cp -> cp.getCourse().getId())
                .collect(Collectors.toSet());
        final Set<Integer> enrolledIds = enrolledCourseIds; // For lambda access

        return allCourses.stream()

                // 5. Visibility rule
                .filter(c -> {
                    boolean isPublic = c.getVisibility() == Course.Visibility.PUBLIC;
                    boolean isOwner = Objects.equals(c.getOwner().getId(), user.getId());
                    boolean isShared = sharedCourseIds.contains(c.getId());
                    return isPublic || isOwner || isShared;
                })

                // 6. Shared with me
                .filter(c -> sharedWithMe == null || !sharedWithMe ||
                        sharedCourseIds.contains(c.getId()))

                // 7. Enrolled
                .filter(c -> enrolled == null || !enrolled ||
                        enrolledIds.contains(c.getId()))

                // 8. Forkable
                .filter(c -> forkable == null ||
                        (c.getForkable() != null && c.getForkable().equals(forkable)))

                // 9. Tags
                .filter(c -> {
                    if (tags == null || tags.isEmpty()) return true;

                    Set<String> courseTagNames = c.getCourseTags().stream()
                            .map(ct -> ct.getTag().getName())
                            .collect(Collectors.toSet());

                    return courseTagNames.containsAll(tags);
                })

                // 10. Search
                .filter(c -> {
                    if (!StringUtils.hasText(search)) return true;
                    String lower = search.toLowerCase();

                    return c.getName().toLowerCase().contains(lower)
                            || (c.getDescription() != null &&
                            c.getDescription().toLowerCase().contains(lower));
                })

                // 11. Sorting
                .sorted((a, b) -> {
                    if ("name".equals(sortBy)) {
                        return "descending".equals(order)
                                ? b.getName().compareTo(a.getName())
                                : a.getName().compareTo(b.getName());
                    }
                    if ("date_created".equals(sortBy)) {
                        return "descending".equals(order)
                                ? b.getCreatedAt().compareTo(a.getCreatedAt())
                                : a.getCreatedAt().compareTo(b.getCreatedAt());
                    }
                    return 0;
                })

                // 12. Map to response
                .map(c -> CourseFilterResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .introduction(c.getIntroduction())
                        .forkable(c.getForkable())
                        .owner(c.getOwner().getUsername())
                        .tags(c.getCourseTags().stream()
                                .map(CourseTag::getTag)
                                .toList())
                        .build()
                )
                .toList();
    }

    // ---------------- FETCH ENROLLED COURSES ----------------
    /**
     * Get all courses the user is enrolled in via CourseProgress
     * @param user the user
     * @return list of courses the user is enrolled in
     */
    public List<Course> getEnrolledCourses(User user) {
        List<CourseProgress> enrolledProgress = courseProgressRepository.findByUserAndEnrolledTrue(user);
        return enrolledProgress.stream()
                .map(CourseProgress::getCourse)
                .collect(Collectors.toList());
    }

    // ---------------- FETCH COURSE WITH CHAPTERS ----------------
    public CourseContentResponse fetchCourseWithChapters(User user, Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        // Check ownership, shared access, or enrollment
        boolean isOwner = course.getOwner().getId().equals(user.getId());
        boolean isShared = courseSharedRepository.findByUserAndCourse(user, course).isPresent();
        boolean isEnrolled = courseProgressRepository.findByUserAndCourse(user, course).isPresent();
        
        if (!isOwner && !isShared && !isEnrolled) {
            throw new SecurityException("User cannot access this course");
        }

        // Load chapters
        List<Chapter> chapters = chapterRepository.findByCourse(course);

        // Map chapters to DTO
        List<CourseContentResponse.Chapter> chapterResponses = chapters.stream()
                .map(ch -> CourseContentResponse.Chapter.builder()
                        .id(ch.getId())
                        .idx(ch.getIdx())
                        .name(ch.getName())
                        .description(ch.getDescription())
                        .icon(ch.getIcon())
                        .build())
                .toList();

        // Load course tags
        List<String> tags = course.getCourseTags().stream()
                .map(ct -> ct.getTag().getName())
                .toList();

        // Build final DTO
        return CourseContentResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .introduction(course.getIntroduction())
                .forkable(course.getForkable())
                .owner(course.getOwner().getUsername())
                .tags(tags)
                .chapters(chapterResponses)
                .build();
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

        boolean alreadyShared = courseSharedRepository.findByUserAndCourse(targetUser, course).isPresent();
        if (!alreadyShared) {
            CourseShared shared = new CourseShared();
            shared.setUser(targetUser);
            shared.setCourse(course);
            shared.setId(new CourseSharedId(targetUser.getId(), course.getId())); // MUST set this

            courseSharedRepository.save(shared);
        }
    }

    // ---------------- HELPER METHODS ----------------
    private boolean hasAccessToCourse(Course course, User user) {
        boolean isOwner = course.getOwner().getId().equals(user.getId());
        boolean isShared = courseSharedRepository.findByUserAndCourse(user, course).isPresent();
        boolean isEnrolled = courseProgressRepository.findByUserAndCourse(user, course).isPresent();
        return isOwner || isShared || isEnrolled;
    }

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

            // ------------------ COPY CHAPTER ------------------
            Chapter newChapter = com.darauy.quark.entity.courses.Chapter.builder()
                    .name(ch.getName())
                    .idx(ch.getIdx())
                    .description(ch.getDescription())
                    .icon(ch.getIcon())
                    .course(forked)
                    .build();
            chapterRepository.save(newChapter);


            // ------------------ COPY LESSONS ------------------
            List<Lesson> lessons = lessonRepository.findByChapter(ch);

            for (Lesson l : lessons) {
                Lesson newLesson = Lesson.builder()
                        .name(l.getName())
                        .idx(l.getIdx())
                        .description(l.getDescription())
                        .icon(l.getIcon())
                        .finishMessage(l.getFinishMessage())
                        .chapter(newChapter)
                        .build();
                lessonRepository.save(newLesson);


                // ------------------ COPY PAGES ------------------
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


            // ------------------ COPY ACTIVITIES ------------------
            List<Activity> activities = activityRepository.findByChapter(ch);

            for (Activity a : activities) {
                Activity newActivity = Activity.builder()
                        .name(a.getName())
                        .idx(a.getIdx())
                        .description(a.getDescription())
                        .icon(a.getIcon())
                        .finishMessage(a.getFinishMessage())
                        .chapter(newChapter)
                        .build();
                activityRepository.save(newActivity);


                // ------------------ COPY SECTIONS ------------------
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
