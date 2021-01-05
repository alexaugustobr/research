package com.github.paulosalonso.research.adapter.controller.mapper;

import com.github.paulosalonso.research.adapter.controller.dto.QuestionCriteriaDTO;
import com.github.paulosalonso.research.adapter.controller.dto.QuestionInputDTO;
import com.github.paulosalonso.research.domain.Question;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDTOMapperTest {

    private QuestionDTOMapper mapper = new QuestionDTOMapper();

    @Test
    public void givenAQuestionWhenMapThenReturnDTO() {
        var question = Question.builder()
                .id(UUID.randomUUID())
                .sequence(1)
                .description("description")
                .multiSelect(true)
                .build();

        var dto = mapper.toDTO(question);

        assertThat(dto.getId()).isEqualTo(question.getId());
        assertThat(dto.getSequence()).isEqualTo(question.getSequence());
        assertThat(dto.getDescription()).isEqualTo(question.getDescription());
        assertThat(dto.getMultiSelect()).isEqualTo(question.getMultiSelect());
    }

    @Test
    public void givenAQuestionInputDTOWhenMapThenReturnQuestion() {
        var questionInputDTO = QuestionInputDTO.builder()
                .description("description")
                .multiSelect(true)
                .build();

        var question = mapper.toDomain(questionInputDTO);

        assertThat(question.getId()).isNull();
        assertThat(question.getDescription()).isEqualTo(questionInputDTO.getDescription());
        assertThat(question.getMultiSelect()).isEqualTo(questionInputDTO.getMultiSelect());
    }

    @Test
    public void givenAQuestionCriteriaDTOWhenMapThenReturnQuestionCriteria() {
        var questionCriteriaDTO = QuestionCriteriaDTO.builder()
                .description("description")
                .multiSelect(true)
                .build();

        var questionCriteria = mapper.toDomain(questionCriteriaDTO);

        assertThat(questionCriteria.getDescription()).isEqualTo(questionCriteriaDTO.getDescription());
        assertThat(questionCriteria.getMultiSelect()).isEqualTo(questionCriteriaDTO.getMultiSelect());
    }
}
