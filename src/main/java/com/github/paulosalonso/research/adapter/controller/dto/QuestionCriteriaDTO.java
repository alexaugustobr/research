package com.github.paulosalonso.research.adapter.controller.dto;

import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class QuestionCriteriaDTO {
    private String description;
    private Boolean multiSelect;
}