package com.darauy.quark.entity.progress;

import com.darauy.quark.entity.courses.Chapter;
import com.darauy.quark.entity.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "chapter_progress",
        indexes = {
                @Index(name = "idx_chapter_progress_user", columnList = "user_id"),
                @Index(name = "idx_chapter_progress_chapter", columnList = "chapter_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterProgress {

    @EmbeddedId
    private ChapterProgressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chapterId")
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Builder
    public ChapterProgress(User user, Chapter chapter) {
        this.user = user;
        this.chapter = chapter;
        this.id = new ChapterProgressId(user.getId(), chapter.getId());
    }
}
