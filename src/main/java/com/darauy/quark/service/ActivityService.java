package com.darauy.quark.service;

import com.darauy.quark.dto.request.ActivityRequest;
import com.darauy.quark.dto.response.ActivityContentResponse;
import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.courses.lesson.Lesson;
import com.darauy.quark.repository.ActivityRepository;
import com.darauy.quark.repository.ChapterRepository;
import com.darauy.quark.repository.SectionRepository;
import com.darauy.quark.repository.CourseProgressRepository;
import com.darauy.quark.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ChapterRepository chapterRepository;
    private final ActivityRepository activityRepository;
    private final SectionRepository sectionRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ------------------ Add Activity ------------------
    public void addActivity(Integer chapterId, Integer userId, ActivityRequest req) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter's course");
        }

        validateRequest(req);

        int nextIdx = computeNextIdx(chapter);

        Activity activity = Activity.builder()
                .name(req.getName())
                .description(req.getDescription())
                .icon(req.getIcon())
                .finishMessage(req.getFinishMessage())
                .ruleset(serializeRuleset(req.getRuleset()))
                .idx(nextIdx)
                .version(1)
                .chapter(chapter)
                .build();

        activityRepository.save(activity);
    }

    // ------------------ Edit Activity ------------------
    public void editActivity(Integer activityId, Integer userId, ActivityRequest req) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        if (!activity.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("Activity not owned by this user");
        }

        validateRequest(req);

        activity.setName(req.getName());
        activity.setDescription(req.getDescription());
        activity.setIcon(req.getIcon());
        activity.setFinishMessage(req.getFinishMessage());
        activity.setRuleset(serializeRuleset(req.getRuleset()));
        activity.setVersion(activity.getVersion() + 1);

        activityRepository.save(activity);
    }

    // ------------------ Delete Activity ------------------
    public void deleteActivity(Integer activityId, Integer userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        if (!activity.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("Activity not owned by this user");
        }

        Integer chapterId = activity.getChapter().getId();
        int removedIdx = activity.getIdx();

        activityRepository.delete(activity);

        // reorder idx's
        List<Activity> siblings = activityRepository.findByChapterIdOrderByIdx(chapterId);
        int counter = 1;
        for (Activity a : siblings) {
            a.setIdx(counter++);
            activityRepository.save(a);
        }
    }

    // ------------------ Fetch Activity ------------------
    public ActivityContentResponse fetchActivity(Integer activityId, Integer userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        if (!activity.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this activity's course");
        }

        List<Section> sections = sectionRepository.findByActivityId(activityId)
                .stream()
                .sorted(Comparator.comparingInt(Section::getIdx))
                .toList();

        return ActivityContentResponse.builder()
                .id(activity.getId())
                .idx(activity.getIdx())
                .name(activity.getName())
                .description(activity.getDescription())
                .icon(activity.getIcon())
                .finishMessage(activity.getFinishMessage())
                .ruleset(deserializeRuleset(activity.getRuleset()))
                .sections(
                        sections.stream().map(s -> {
                            ActivityContentResponse.Section dto = new ActivityContentResponse.Section();
                            dto.setId(s.getId());
                            dto.setIdx(s.getIdx());
                            return dto;
                        }).toList()
                )
                .build();
    }

    // ------------------ Validation ------------------
    private void validateRequest(ActivityRequest r) {
        if (r.getName() == null || r.getName().length() < 10 || r.getName().length() > 255)
            throw new IllegalArgumentException("Invalid activity name");

        if (r.getDescription() != null && r.getDescription().length() > 255)
            throw new IllegalArgumentException("Invalid description");
    }

    // ------------------ Ruleset JSON Handling ------------------
    private String serializeRuleset(ActivityRequest.Ruleset ruleset) {
        try {
            return ruleset == null ? "{}" : objectMapper.writeValueAsString(ruleset);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ruleset format");
        }
    }

    private ActivityContentResponse.Ruleset deserializeRuleset(String json) {
        try {
            return objectMapper.readValue(json, ActivityContentResponse.Ruleset.class);
        } catch (Exception e) {
            return new ActivityContentResponse.Ruleset(); // fallback
        }
    }

    private int computeNextIdx(Chapter chapter) {
        int maxLessonIdx = chapter.getLessons() == null ? 0 :
                chapter.getLessons().stream()
                        .mapToInt(Lesson::getIdx)
                        .max().orElse(0);

        int maxActivityIdx = chapter.getActivities() == null ? 0 :
                chapter.getActivities().stream()
                        .mapToInt(Activity::getIdx)
                        .max().orElse(0);

        return Math.max(maxLessonIdx, maxActivityIdx) + 1;
    }

    private boolean hasAccessToCourse(com.darauy.quark.entity.courses.Course course, Integer userId) {
        boolean isOwner = course.getOwnerId().equals(userId);
        if (isOwner) return true;
        
        var user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        
        boolean isEnrolled = courseProgressRepository.findByUserAndCourse(user.get(), course).isPresent();
        return isEnrolled;
    }

}
