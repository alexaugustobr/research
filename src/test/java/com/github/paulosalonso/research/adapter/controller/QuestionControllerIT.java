package com.github.paulosalonso.research.adapter.controller;

import com.github.paulosalonso.research.adapter.controller.dto.QuestionInputDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static com.github.paulosalonso.research.adapter.controller.OptionCreator.createOption;
import static com.github.paulosalonso.research.adapter.controller.QuestionCreator.createQuestion;
import static com.github.paulosalonso.research.adapter.controller.ResearchCreator.createResearch;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.*;

public class QuestionControllerIT extends BaseIT {

    @Test
    public void whenGetWithoutOptionsThenReturnOk() {
        var research = createResearch();

        var body = QuestionInputDTO.builder()
                .description("description")
                .multiSelect(false)
                .build();

        String questionId = givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(body)
                .post("/researches/{researchId}/questions", research.getId())
                .path("id");

        givenAuthenticatedAdmin()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/questions/{questionId}", research.getId(), questionId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(questionId))
                .body("description", equalTo(body.getDescription()))
                .body("multiSelect", equalTo(body.getMultiSelect()))
                .body("$", not(hasKey("options")));
    }

    @Test
    public void whenGetWithOptionsThenReturnOk() {
        var research = createResearch();

        var body = QuestionInputDTO.builder()
                .description("description")
                .multiSelect(false)
                .build();

        String questionId = givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(body)
                .post("/researches/{researchId}/questions", research.getId())
                .path("id");

        var option = createOption(UUID.fromString(questionId));

        givenAuthenticatedAdmin()
                .accept(JSON)
                .queryParam("fillOptions", true)
                .when()
                .get("/researches/{researchId}/questions/{questionId}", research.getId(), questionId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(questionId))
                .body("description", equalTo(body.getDescription()))
                .body("multiSelect", equalTo(body.getMultiSelect()))
                .body("options", hasSize(1))
                .body("options.id", contains(option.getId().toString()));
    }

    @Test
    public void givenANonexistentResearchIdWhenGetThenReturnNotFound() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        givenAuthenticatedAdmin()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/questions/{questionId}", UUID.randomUUID(), question.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenANonexistentQuestionIdWhenGetThenReturnNotFound() {
        var research = createResearch();

        givenAuthenticatedAdmin()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/questions/{questionId}", research.getId(), UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenAnInvalidResearchUUIDWhenGetThenReturnBadRequest() {
        givenAuthenticatedAdmin()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/questions/{questionId}", "invalid-uuid", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", equalTo("'invalid-uuid' is an invalid value for the 'researchId' URL parameter. Required type is 'UUID'."))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenAnInvalidQuestionUUIDWhenGetThenReturnBadRequest() {
        givenAuthenticatedAdmin()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/questions/{questionId}", UUID.randomUUID(), "invalid-uuid")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", equalTo("'invalid-uuid' is an invalid value for the 'questionId' URL parameter. Required type is 'UUID'."))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void whenSearchWithoutParametersThenReturnAll() {
        truncateDatabase();

        var researchId = createResearch().getId();

        createQuestion(researchId);
        createQuestion(researchId);

        givenAuthenticatedAdmin()
                .accept(JSON)
                .when()
                .get("/researches/{researchId}/questions", researchId.toString())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2));
    }

    @Test
    public void whenSearchWithDescriptionParameterThenReturnFiltered() {
        truncateDatabase();

        var research = createResearch();

        createQuestion(research.getId(), QuestionInputDTO.builder()
                .description("description-a")
                .multiSelect(true)
                .build());

        var questionB = createQuestion(research.getId(), QuestionInputDTO.builder()
                .description("description-b")
                .multiSelect(true)
                .build());

        givenAuthenticatedAdmin()
                .accept(JSON)
                .queryParam("description", questionB.getDescription())
                .when()
                .get("/researches/{researchId}/questions", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("id", contains(questionB.getId().toString()));
    }

    @Test
    public void whenSearchWithMultiSelectParameterThenReturnFiltered() {
        truncateDatabase();

        var research = createResearch();

        createQuestion(research.getId(), QuestionInputDTO.builder()
                .description("description-a")
                .multiSelect(false)
                .build());

        var questionB = createQuestion(research.getId(), QuestionInputDTO.builder()
                .description("description-b")
                .multiSelect(true)
                .build());

        givenAuthenticatedAdmin()
                .accept(JSON)
                .queryParam("multiSelect", questionB.getMultiSelect())
                .when()
                .get("/researches/{researchId}/questions", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("id", contains(questionB.getId().toString()));
    }

    @Test
    public void whenSearchWithAllParametersThenReturnFiltered() {
        truncateDatabase();

        var research = createResearch();

        createQuestion(research.getId(), QuestionInputDTO.builder()
                .description("description-a")
                .multiSelect(false)
                .build());

        var questionB = createQuestion(research.getId(), QuestionInputDTO.builder()
                .description("description-b")
                .multiSelect(true)
                .build());

        givenAuthenticatedAdmin()
                .accept(JSON)
                .queryParam("description", questionB.getDescription())
                .queryParam("multiSelect", questionB.getMultiSelect())
                .when()
                .get("/researches/{researchId}/questions", research.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("id", contains(questionB.getId().toString()));
    }

    @Test
    public void whenCreateThenReturnCreated() {
        var research = createResearch();

        var body = QuestionInputDTO.builder()
                .description("description")
                .multiSelect(true)
                .build();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(body)
                .when()
                .post("/researches/{researchId}/questions", research.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("description", equalTo(body.getDescription()))
                .body("multiSelect", equalTo(body.getMultiSelect()));
    }

    @Test
    public void whenCreateWithNonexistentResearchIdThenReturnNotFound() {
        var body = QuestionInputDTO.builder()
                .description("description")
                .multiSelect(false)
                .build();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(body)
                .when()
                .post("/researches/{researchId}/questions", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void whenCreateWithNullRequiredValueThenReturnBadRequest() {
        var research = createResearch();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .header("Accept-Language", "en-US")
                .body(QuestionInputDTO.builder().build())
                .when()
                .post("/researches/{researchId}/questions", research.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Invalid field(s)"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("fields", hasSize(2))
                .body("fields.name", hasItems("description", "multiSelect"))
                .body("fields.message", hasItems("must not be blank", "must not be null"));
    }

    @Test
    public void whenUpdateThenReturnOk() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        var updateBody = QuestionInputDTO.builder()
                .description(question.getDescription() + " updated")
                .multiSelect(!question.getMultiSelect())
                .build();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(updateBody)
                .when()
                .put("researches/{researchId}/questions/{questionId}", research.getId(), question.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(question.getId().toString()))
                .body("description", equalTo(updateBody.getDescription()))
                .body("multiSelect", equalTo(updateBody.getMultiSelect()));
    }

    @Test
    public void givenANonexistentResearchIdWhenUpdateThenReturnNotFound() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        var updateBody = QuestionInputDTO.builder()
                .description(question.getDescription() + " updated")
                .multiSelect(!question.getMultiSelect())
                .build();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(updateBody)
                .when()
                .put("researches/{researchId}/questions/{questionId}", UUID.randomUUID(), question.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenANonexistentQuestionIdWhenUpdateThenReturnNotFound() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        var updateBody = QuestionInputDTO.builder()
                .description(question.getDescription() + " updated")
                .multiSelect(!question.getMultiSelect())
                .build();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(updateBody)
                .when()
                .put("researches/{researchId}/questions/{questionId}", research.getId(), UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void whenUpdateWithNullRequiredValueThenReturnBadRequest() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .header("Accept-Language", "en-US")
                .body(QuestionInputDTO.builder().build())
                .when()
                .put("researches/{researchId}/questions/{questionId}", research.getId(), question.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Invalid field(s)"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("fields", hasSize(2))
                .body("fields.name", hasItems("description", "multiSelect"))
                .body("fields.message", hasItems("must not be blank", "must not be null"));
    }

    @Test
    public void givenAnInvalidResearchUUIDWhenUpdateThenReturnBadRequest() {
        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(QuestionInputDTO.builder().build())
                .when()
                .put("researches/{researchId}/questions/{questionId}", "invalid-uuid", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", equalTo("'invalid-uuid' is an invalid value for the 'researchId' URL parameter. Required type is 'UUID'."))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenAnInvalidQuestionUUIDWhenUpdateThenReturnBadRequest() {
        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .body(QuestionInputDTO.builder().build())
                .when()
                .put("researches/{researchId}/questions/{questionId}", UUID.randomUUID(), "invalid-uuid")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", equalTo("'invalid-uuid' is an invalid value for the 'questionId' URL parameter. Required type is 'UUID'."))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void whenDeleteThenReturnNoContent() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .when()
                .delete("/researches/{researchId}/questions/{questionId}", research.getId(), question.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    public void givenANonexistentResearchIdWhenDeleteThenReturnNotFound() {
        var research = createResearch();
        var question = createQuestion(research.getId());

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .when()
                .delete("/researches/{researchId}/questions/{questionId}", UUID.randomUUID(), question.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenANonexistentQuestionIdWhenDeleteThenReturnNotFound() {
        var research = createResearch();

        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .when()
                .delete("/researches/{researchId}/questions/{questionId}", research.getId(), UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("status", equalTo(HttpStatus.NOT_FOUND.value()))
                .body("message", equalTo("Requested resource not found"))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenAnInvalidResearchUUIDWhenDeleteThenReturnBadRequest() {
        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .when()
                .delete("/researches/{researchId}/questions/{questionId}", "invalid-uuid", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", equalTo("'invalid-uuid' is an invalid value for the 'researchId' URL parameter. Required type is 'UUID'."))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }

    @Test
    public void givenAnInvalidQuestionUUIDWhenDeleteThenReturnBadRequest() {
        givenAuthenticatedAdmin()
                .contentType(JSON)
                .accept(JSON)
                .when()
                .delete("/researches/{researchId}/questions/{questionId}", UUID.randomUUID(), "invalid-uuid")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(HttpStatus.BAD_REQUEST.value()))
                .body("message", equalTo("'invalid-uuid' is an invalid value for the 'questionId' URL parameter. Required type is 'UUID'."))
                .body("timestamp", matchesRegex(ISO_8601_REGEX))
                .body("$", not(hasKey("fields")));
    }
}
