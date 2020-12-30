package com.github.paulosalonso.research.application.controller;

import com.github.paulosalonso.research.application.dto.ResearchAnswerInputDTO;
import com.github.paulosalonso.research.application.dto.ResearchAnswerInputDTO.QuestionAnswerInputDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static com.github.paulosalonso.research.application.AnswerCreator.createAnswer;
import static com.github.paulosalonso.research.application.OptionCreator.createOption;
import static com.github.paulosalonso.research.application.QuestionCreator.createQuestion;
import static com.github.paulosalonso.research.application.ResearchCreator.createResearch;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

public class AnswerControllerIT extends BaseIT {

    @Test
    public void whenCreateThenReturnNoContent() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        var answer = ResearchAnswerInputDTO.builder()
                .answer(QuestionAnswerInputDTO.builder()
                        .questionId(UUID.fromString(question.getId()))
                        .optionId(UUID.fromString(option.getId()))
                        .build())
                .build();

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(answer)
                .when()
                .post("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void whenCreateWithoutAllResearchQuestionsThenReturnBadRequest() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        var answer = ResearchAnswerInputDTO.builder()
                .answer(QuestionAnswerInputDTO.builder()
                        .questionId(UUID.fromString(question.getId()))
                        .optionId(UUID.fromString(option.getId()))
                        .build())
                .build();

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(answer)
                .when()
                .post("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void whenCreateWithNonexistentResearchIdThenReturnBadRequest() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        var answer = ResearchAnswerInputDTO.builder()
                .answer(QuestionAnswerInputDTO.builder()
                        .questionId(UUID.fromString(question.getId()))
                        .optionId(UUID.fromString(option.getId()))
                        .build())
                .build();

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(answer)
                .when()
                .post("/researches/{researchId}/answers", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void whenCreateWithNonexistentQuestionIdThenReturnBadRequest() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        var answer = ResearchAnswerInputDTO.builder()
                .answer(QuestionAnswerInputDTO.builder()
                        .questionId(UUID.randomUUID())
                        .optionId(UUID.fromString(option.getId()))
                        .build())
                .build();

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(answer)
                .when()
                .post("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void whenCreateWithNonexistentOptionIdThenReturnBadRequest() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));

        var answer = ResearchAnswerInputDTO.builder()
                .answer(QuestionAnswerInputDTO.builder()
                        .questionId(UUID.fromString(question.getId()))
                        .optionId(UUID.randomUUID())
                        .build())
                .build();

        given()
                .contentType(JSON)
                .accept(JSON)
                .body(answer)
                .when()
                .post("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void whenSearchWithoutParametersThenReturnAllAnswersSummarized() {
        truncateDatabase();

        var researchA = createResearch();
        var questionAA = createQuestion(UUID.fromString(researchA.getId()));
        var optionAAA = createOption(UUID.fromString(questionAA.getId()));
        var optionAAB = createOption(UUID.fromString(questionAA.getId()));
        var questionAB = createQuestion(UUID.fromString(researchA.getId()));
        var optionABA = createOption(UUID.fromString(questionAB.getId()));
        var optionABB = createOption(UUID.fromString(questionAB.getId()));

        createAnswer(UUID.fromString(researchA.getId()), Map.of(
                UUID.fromString(questionAA.getId()), UUID.fromString(optionAAA.getId()),
                UUID.fromString(questionAB.getId()), UUID.fromString(optionABA.getId())));

        createAnswer(UUID.fromString(researchA.getId()), Map.of(
                UUID.fromString(questionAA.getId()), UUID.fromString(optionAAA.getId()),
                UUID.fromString(questionAB.getId()), UUID.fromString(optionABA.getId())));

        createAnswer(UUID.fromString(researchA.getId()), Map.of(
                UUID.fromString(questionAA.getId()), UUID.fromString(optionAAB.getId()),
                UUID.fromString(questionAB.getId()), UUID.fromString(optionABB.getId())));

        given()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/answers", researchA.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(researchA.getId()))
                .body("title", equalTo(researchA.getTitle()))
                .body("criteria", notNullValue())
                .body("criteria.dateFrom", nullValue())
                .body("criteria.dateTo", nullValue())
                .body("criteria.questionId", nullValue())
                .body("questions", hasSize(2))
//                .body("questions.id", contains(questionAA.getId(), questionAB.getId())) // TODO - The groupingBy stream collector reverses the content of list. Check it.
//                .body("questions.description", contains(questionAA.getDescription(), questionAB.getDescription()))
                .body("questions[0].options", hasSize(2))
//                .body("questions[0].options.id", contains(optionAAA.getId(), optionAAB.getId()))
//                .body("questions[0].options.sequence", contains(optionAAA.getSequence(), optionAAB.getSequence()))
                .body("questions[1].options", hasSize(2));

        // Because it is not yet possible to ensure the sequence it is not possible testing response body, as the sequence varies when grouping the results.
        // TODO - Ensure questions and options sequence
    }

    @Test
    public void whenSearchWithQuestionIdParameterThenReturnFiltered() {
        truncateDatabase();

        var research = createResearch();
        var questionA = createQuestion(UUID.fromString(research.getId()));
        var optionAA = createOption(UUID.fromString(questionA.getId()));
        createOption(UUID.fromString(questionA.getId()));
        var questionB = createQuestion(UUID.fromString(research.getId()));
        var optionBA = createOption(UUID.fromString(questionB.getId()));

        createAnswer(UUID.fromString(research.getId()), Map.of(
                UUID.fromString(questionA.getId()), UUID.fromString(optionAA.getId()),
                UUID.fromString(questionB.getId()), UUID.fromString(optionBA.getId())));

        given()
                .accept(JSON)
                .queryParam("questionId", questionA.getId())
                .when()
                .get("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].options[0].amount", equalTo(1))
                .body("questions[0].options[1].amount", equalTo(0))
                .body("questions[1].options[0].amount", equalTo(0));
    }

    @Test
    public void whenSearchWithNonexistentQuestionIdThenReturnEmpty() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        createOption(UUID.fromString(question.getId()));

        given()
                .accept(JSON)
                .queryParam("questionId", UUID.randomUUID())
                .when()
                .get("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].options[0].amount", equalTo(0));
    }

    @Test
    public void whenSearchWithDateParametersThenReturnFiltered() throws InterruptedException {
        truncateDatabase();

        var dateFrom = OffsetDateTime.now();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        createAnswer(UUID.fromString(research.getId()), Map.of(
                UUID.fromString(question.getId()), UUID.fromString(option.getId())));

        var dateTo = OffsetDateTime.now();

        Thread.sleep(1000);

        createAnswer(UUID.fromString(research.getId()), Map.of(
                UUID.fromString(question.getId()), UUID.fromString(option.getId())));

        given()
                .accept(JSON)
                .queryParam("dateFrom", ISO_DATE_TIME.format(dateFrom))
                .queryParam("dateTo", ISO_DATE_TIME.format(dateTo))
                .when()
                .get("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].options[0].amount", equalTo(1));
    }

    @Test
    public void whenSearchWithDateParametersWithPreviousPeriodThenReturnFiltered() {
        truncateDatabase();

        var dateTo = OffsetDateTime.now();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        createAnswer(UUID.fromString(research.getId()), Map.of(
                UUID.fromString(question.getId()), UUID.fromString(option.getId())));

        given()
                .accept(JSON)
                .queryParam("dateTo", ISO_DATE_TIME.format(dateTo))
                .when()
                .get("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].options[0].amount", equalTo(0));
    }

    @Test
    public void whenSearchWithDateParametersWithLaterPeriodThenReturnFiltered() {
        truncateDatabase();

        var research = createResearch();
        var question = createQuestion(UUID.fromString(research.getId()));
        var option = createOption(UUID.fromString(question.getId()));

        createAnswer(UUID.fromString(research.getId()), Map.of(
                UUID.fromString(question.getId()), UUID.fromString(option.getId())));

        var dateFrom = OffsetDateTime.now();

        given()
                .accept(JSON)
                .queryParam("dateFrom", ISO_DATE_TIME.format(dateFrom))
                .when()
                .get("/researches/{researchId}/answers", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("questions[0].options[0].amount", equalTo(0));
    }

    @Test
    public void whenSearchWithNonexistentResearchIdThenReturnNotFound() {
        truncateDatabase();

        given()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/answers", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
