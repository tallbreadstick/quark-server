package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.activity.MCQQuestion;
import com.darauy.quark.entity.courses.activity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MCQQuestionRepository extends JpaRepository<MCQQuestion, Integer> {
    List<MCQQuestion> findBySectionOrderByIdxAsc(Section section);
}
