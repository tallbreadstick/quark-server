package com.darauy.quark.service;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.repository.ActivityRepository;
import com.darauy.quark.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ChapterRepository chapterRepository;

    // Add Activity
    public Activity addActivity(Integer chapterId, Activity activity, Integer userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found"));

        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this chapter");
        }

        // Determine idx
        int maxIdx = activityRepository.findByChapter(chapter).stream()
                .mapToInt(Activity::getIdx).max().orElse(0);
        activity.setIdx(maxIdx + 1);
        activity.setChapter(chapter);

        activity.setVersion(1);

        return activityRepository.save(activity);
    }

    // Edit Activity
    public Activity editActivity(Integer activityId, Activity updated, Integer userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        if (!activity.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this activity's chapter/course");
        }

        activity.setName(updated.getName());
        activity.setDescription(updated.getDescription());
        activity.setIcon(updated.getIcon());
        activity.setRuleset(updated.getRuleset());
        activity.setFinishMessage(updated.getFinishMessage());
        activity.setVersion(activity.getVersion() + 1);

        return activityRepository.save(activity);
    }

    // Delete Activity
    @Transactional
    public void deleteActivity(Integer activityId, Integer userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        Chapter chapter = activity.getChapter();
        if (!chapter.getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this activity's chapter/course");
        }

        activityRepository.delete(activity);

        // Reorder remaining lessons/activities
        List<Activity> remaining = activityRepository.findByChapter(chapter);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setIdx(i + 1);
        }
        activityRepository.saveAll(remaining);
    }

    // Fetch Activity with sections (if needed)
    public Activity fetchActivity(Integer activityId, Integer userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        if (!activity.getChapter().getCourse().getOwnerId().equals(userId)) {
            throw new SecurityException("User does not own this activity's chapter/course");
        }

        // Force load sections if lazy (optional)
        // activity.getSections().size();

        return activity;
    }
}
