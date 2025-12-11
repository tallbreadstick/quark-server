package com.darauy.quark.repository;

import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.courses.activity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
    List<TestCase> findBySectionOrderByIdxAsc(Section section);
    List<TestCase> findBySectionAndHiddenFalseOrderByIdxAsc(Section section);
}
