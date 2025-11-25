package com.darauy.quark.dto.response;

import com.darauy.quark.entity.courses.lesson.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageContentResponse {
    private Page.Renderer renderer;
    private String content;
}
