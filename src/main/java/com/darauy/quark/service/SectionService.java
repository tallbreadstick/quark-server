package com.darauy.quark.service;

import com.darauy.quark.dto.SectionRequest;
import com.darauy.quark.entity.courses.activity.Activity;
import com.darauy.quark.entity.courses.activity.Section;
import com.darauy.quark.entity.users.User;
import com.darauy.quark.repository.ActivityRepository;
import com.darauy.quark.repository.SectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ActivityRepository activityRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Section createSection(User user, Integer activityId, SectionRequest request) throws Exception {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        // TODO: check activity belongs to user's course

        // Serialize DTO into JSON string
        String contentJson = objectMapper.writeValueAsString(request);

        Section section = Section.builder()
                .activity(activity)
                .idx(determineNextIdx(activity))
                .content(contentJson)
                .build();

        return sectionRepository.save(section);
    }

    @Transactional
    public Section editSection(User user, Integer sectionId, SectionRequest request) throws Exception {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        // TODO: check section belongs to user's activity/course

        String contentJson = objectMapper.writeValueAsString(request);
        section.setContent(contentJson);

        return sectionRepository.save(section);
    }

    @Transactional
    public void deleteSection(User user, Integer sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        // TODO: check section belongs to user's activity/course

        Activity activity = section.getActivity();
        sectionRepository.delete(section);

        // Reorder idx of remaining sections
        List<Section> siblings = sectionRepository.findByActivity(activity);
        siblings.sort(Comparator.comparing(Section::getIdx));
        for (int i = 0; i < siblings.size(); i++) {
            siblings.get(i).setIdx(i + 1);
        }
        sectionRepository.saveAll(siblings);
    }

    @Transactional
    public List<Section> reorderSections(User user, Integer activityId, List<Integer> sectionIds) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NoSuchElementException("Activity not found"));

        // TODO: check activity belongs to user's course

        List<Section> sections = sectionRepository.findByActivity(activity);
        if (sections.size() != sectionIds.size() ||
                !sections.stream().map(Section::getId).collect(Collectors.toList()).containsAll(sectionIds)) {
            throw new IllegalArgumentException("Invalid section IDs");
        }

        for (int i = 0; i < sectionIds.size(); i++) {
            int finalI = i;
            Section s = sections.stream().filter(sec -> sec.getId().equals(sectionIds.get(finalI))).findFirst().get();
            s.setIdx(i + 1);
        }

        return sectionRepository.saveAll(sections);
    }

    @Transactional(readOnly = true)
    public Section fetchSection(User user, Integer sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NoSuchElementException("Section not found"));

        // TODO: check section belongs to user's activity/course

        return section;
    }

    private Integer determineNextIdx(Activity activity) {
        List<Section> sections = sectionRepository.findByActivity(activity);
        return sections.stream().map(Section::getIdx).max(Integer::compare).orElse(0) + 1;
    }
}
