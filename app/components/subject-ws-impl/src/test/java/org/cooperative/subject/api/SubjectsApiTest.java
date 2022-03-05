package org.cooperative.subject.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cooperative.subject.StubSubjectService;
import org.cooperative.subject.SubjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@WebFluxTest(SubjectsApiController.class)
public class SubjectsApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private StubSubjectService service;

    @Configuration
    public static class TestConfig {
        @Bean
        public SubjectsApiController subjectsApiController(SubjectService subjectService) {
            return new SubjectsApiController(subjectService);
        }

        @Bean
        public SubjectService service() {
            return new StubSubjectService();
        }

        @Bean
        public SubjectsApiErrorHandler subjectsApiErrorHandler() {
            return new SubjectsApiErrorHandler();
        }
    }

    @AfterEach
    public void afterTest() {
        service.deleteAllSubjects();
    }

    @Test
    public void testGetSubjectsNoSubjects() throws Exception {
        webTestClient.get()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SubjectApi.class).hasSize(0);
    }

    @Test
    public void testGetSubjectsSomeSubjects() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name0"));
        service.createSubject(org.cooperative.subject.Subject.of(1, "name1"));

        webTestClient.get()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .header("ContentType", MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SubjectApi.class)
                        .contains(SubjectApi.of(0, "name0"),
                                SubjectApi.of(1, "name1"));
    }

    @Test
    public void testAddSubjectSuccess() throws Exception {
        webTestClient.post()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(SubjectApi.of(0, "name")))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/subjects/0")
                .expectBody().json(mapper.writeValueAsString(SubjectApi.of(0, "name")));

        Optional<org.cooperative.subject.Subject> subject = service.getSubjectById(0);
        assertTrue(subject.isPresent());
        assertEquals(org.cooperative.subject.Subject.of(0, "name"), subject.get());
    }

    @Test
    public void testAddSubjectWrongFormat() throws Exception {
        webTestClient.post()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.empty())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.error").isNotEmpty();
    }

    @Test
    public void testAddSubjectAlreadyExists() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        webTestClient.post()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(SubjectApi.of(0, "name")))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void testUpdateSubjectSuccess() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        webTestClient.put()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(SubjectApi.of(0, "updated")))
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(mapper.writeValueAsString(SubjectApi.of(0, "updated")));

        Optional<org.cooperative.subject.Subject> subject = service.getSubjectById(0);
        assertTrue(subject.isPresent());
        assertEquals(org.cooperative.subject.Subject.of(0, "updated"), subject.get());
    }

    @Test
    public void testUpdateSubjectWrongFormat() throws Exception {
        webTestClient.put()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.empty())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.error").isNotEmpty();
    }

    @Test
    public void testUpdateSubjectNotFound() throws Exception {
        webTestClient.put()
                .uri(URI.create("/subjects"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(SubjectApi.of(0, "name")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testGetSubjectSuccess() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        webTestClient.get()
                .uri(URI.create("/subjects/0"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json(mapper.writeValueAsString(SubjectApi.of(0, "name")));
    }

    @Test
    public void testGetSubjectWrongFormat() throws Exception {
        webTestClient.get()
                .uri(URI.create("/subjects/-1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.error").isNotEmpty();
    }

    @Test
    public void testGetSubjectNotFound() throws Exception {
        webTestClient.get()
                .uri(URI.create("/subjects/0"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testDeleteSubjectSuccess() throws Exception {
        service.createSubject(org.cooperative.subject.Subject.of(0, "name"));

        webTestClient.delete()
                .uri(URI.create("/subjects/0"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void testDeleteSubjectWrongFormat() throws Exception {
        webTestClient.delete()
                .uri(URI.create("/subjects/-1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.error").isNotEmpty();
    }

    @Test
    public void testDeleteSubjectNotFound() throws Exception {
        webTestClient.delete()
                .uri(URI.create("/subjects/0"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
