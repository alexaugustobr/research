package com.github.paulosalonso.research.usecase.question;

import com.github.paulosalonso.research.domain.Option;
import com.github.paulosalonso.research.domain.Question;
import com.github.paulosalonso.research.domain.QuestionCriteria;
import com.github.paulosalonso.research.usecase.port.QuestionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionReadTest {

    @InjectMocks
    private QuestionRead questionRead;

    @Mock
    private QuestionPort port;

    @Test
    public void givenAResearchIdAndAQuestionIdWhenReadWithoutQuestionsThenCallPort() {
        var researchId = UUID.randomUUID();
        var questionId = UUID.randomUUID();

        var toRead = Question.builder()
                .description("description")
                .multiSelect(true)
                .build();

        when(port.read(researchId, questionId)).thenReturn(toRead);

        var result = questionRead.read(researchId, questionId, false);

        assertThat(result).isSameAs(toRead);
        verify(port).read(researchId, questionId);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void givenAResearchIdAndAQuestionIdWhenReadWithQuestionsThenCallPort() {
        var researchId = UUID.randomUUID();
        var questionId = UUID.randomUUID();

        var option = Option.builder()
                .description("description")
                .build();

        var toRead = Question.builder()
                .description("description")
                .multiSelect(true)
                .options(Set.of(option))
                .build();

        when(port.readFetchingOptions(researchId, questionId)).thenReturn(toRead);

        var result = questionRead.read(researchId, questionId, true);

        assertThat(result).isSameAs(toRead);
        verify(port).readFetchingOptions(researchId, questionId);
        verifyNoMoreInteractions(port);
    }

    @Test
    public void givenAResearchIdAndAQuestionCriteriaWhenSearchThenCallPort() {
        var id = UUID.randomUUID();

        var criteria = QuestionCriteria.builder()
                .description("description")
                .multiSelect(true)
                .build();

        var toSearch = List.of(Question.builder()
                .description("description")
                .multiSelect(true)
                .build());

        when(port.search(id, criteria)).thenReturn(toSearch);

        var result = questionRead.search(id, criteria);

        assertThat(result).isSameAs(toSearch);
        verify(port).search(id, criteria);
    }
}
