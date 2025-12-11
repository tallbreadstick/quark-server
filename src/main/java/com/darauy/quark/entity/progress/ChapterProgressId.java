package com.darauy.quark.entity.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "chapter_id")
    private Integer chapterId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChapterProgressId)) return false;
        ChapterProgressId that = (ChapterProgressId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(chapterId, that.chapterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, chapterId);
    }
}
