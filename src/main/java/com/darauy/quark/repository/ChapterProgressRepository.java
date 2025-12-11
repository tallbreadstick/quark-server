package com.darauy.quark.repository;

import com.darauy.quark.entity.progress.ChapterProgress;
import com.darauy.quark.entity.progress.ChapterProgressId;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.entity.courses.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterProgressRepository extends JpaRepository<ChapterProgress, ChapterProgressId> {
    List<ChapterProgress> findByUser(User user);
    List<ChapterProgress> findByChapter(Chapter chapter);
    Optional<ChapterProgress> findByUserAndChapter(User user, Chapter chapter);
}
