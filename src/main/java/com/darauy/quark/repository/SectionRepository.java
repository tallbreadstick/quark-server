package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.activity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Integer> {
    List<Section> findByActivity(Activity activity);

    List<Section> findByActivityId(Integer activityId);
    // Batch method for multiple activities
    List<Section> findByActivityIn(List<Activity> activities);
}
