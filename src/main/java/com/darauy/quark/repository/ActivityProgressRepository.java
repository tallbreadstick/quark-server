package com.darauy.quark.repository;

import com.darauy.quark.entity.progress.ActivityProgress;
import com.darauy.quark.entity.progress.ActivityProgressId;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.entity.courses.activity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityProgressRepository extends JpaRepository<ActivityProgress, ActivityProgressId> {
    List<ActivityProgress> findByUser(User user);
    List<ActivityProgress> findByActivity(Activity activity);
    Optional<ActivityProgress> findByUserAndActivity(User user, Activity activity);
}
