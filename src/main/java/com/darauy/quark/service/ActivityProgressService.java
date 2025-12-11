package com.darauy.quark.service;

import com.darauy.quark.entity.progress.ActivityProgress;
import com.darauy.quark.entity.progress.ActivityProgressId;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.repository.ActivityProgressRepository;
import com.darauy.quark.repository.UserRepository;
import com.darauy.quark.repository.ActivityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ActivityProgressService {

    private final ActivityProgressRepository activityProgressRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    /**
     * Initialize activity progress for a user (tracks section completion)
     * @param userId the user id
     * @param activityId the activity id
     * @return the ActivityProgress
     */
    @Transactional
    public ActivityProgress initializeActivityProgress(Integer userId, Integer activityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        return activityProgressRepository.findByUserAndActivity(user, activity)
                .orElseGet(() -> {
                    ActivityProgress progress = ActivityProgress.builder()
                            .user(user)
                            .activity(activity)
                            .build();
                    return activityProgressRepository.save(progress);
                });
    }

    /**
     * Update completed sections count for an activity
     * @param userId the user id
     * @param activityId the activity id
     * @param completedSections the number of completed sections
     * @return the updated ActivityProgress
     */
    @Transactional
    public ActivityProgress updateCompletedSections(Integer userId, Integer activityId, Integer completedSections) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        ActivityProgress progress = activityProgressRepository.findByUserAndActivity(user, activity)
                .orElseGet(() -> ActivityProgress.builder()
                        .user(user)
                        .activity(activity)
                        .build());

        progress.setCompletedSections(completedSections);
        return activityProgressRepository.save(progress);
    }

    /**
     * Increment completed sections by 1
     * @param userId the user id
     * @param activityId the activity id
     * @return the updated ActivityProgress
     */
    @Transactional
    public ActivityProgress incrementCompletedSections(Integer userId, Integer activityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        ActivityProgress progress = activityProgressRepository.findByUserAndActivity(user, activity)
                .orElseGet(() -> ActivityProgress.builder()
                        .user(user)
                        .activity(activity)
                        .build());

        progress.setCompletedSections(progress.getCompletedSections() + 1);
        return activityProgressRepository.save(progress);
    }

    /**
     * Get activity progress for a user
     * @param userId the user id
     * @param activityId the activity id
     * @return the ActivityProgress
     */
    public ActivityProgress getActivityProgress(Integer userId, Integer activityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        return activityProgressRepository.findByUserAndActivity(user, activity)
                .orElseThrow(() -> new NoSuchElementException("Activity progress not found"));
    }

    /**
     * Get all activity progress for a user
     * @param userId the user id
     * @return list of ActivityProgress entries
     */
    public List<ActivityProgress> getUserActivityProgress(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return activityProgressRepository.findByUser(user);
    }

    /**
     * Delete activity progress for a user
     * @param userId the user id
     * @param activityId the activity id
     */
    @Transactional
    public void deleteActivityProgress(Integer userId, Integer activityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        ActivityProgress progress = activityProgressRepository.findByUserAndActivity(user, activity)
                .orElseThrow(() -> new NoSuchElementException("Activity progress not found"));

        activityProgressRepository.delete(progress);
    }
}
