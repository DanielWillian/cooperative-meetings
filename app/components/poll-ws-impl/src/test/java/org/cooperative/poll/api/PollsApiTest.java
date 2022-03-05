package org.cooperative.poll.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cooperative.poll.Poll;
import org.cooperative.poll.PollService;
import org.cooperative.poll.PollServiceDefault;
import org.cooperative.poll.jpa.PollRepository;
import org.cooperative.poll.jpa.StubPollRepository;
import org.cooperative.subject.StubSubjectService;
import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PollsApiController.class)
public class PollsApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PollService service;

    @Autowired
    private StubPollRepository stubPollRepository;

    @Autowired
    private StubSubjectService stubSubjectService;

    private final String startTimeString = "2021-06-27T12:00:00Z";
    private final String endTimeString = "2021-06-27T13:00:00Z";
    private final OffsetDateTime startTime = OffsetDateTime.parse(startTimeString);
    private final OffsetDateTime endTime = OffsetDateTime.parse(endTimeString);

    @Configuration
    public static class TestConfig {
        @Bean
        public PollsApiController pollsApiController(PollService service) {
            return new PollsApiController(service);
        }

        @Bean
        public PollService service(PollRepository repository, SubjectService service) {
            return new PollServiceDefault(repository, service);
        }

        @Bean
        public PollRepository pollRepository() {
            return new StubPollRepository();
        }

        @Bean
        public SubjectService subjectService() {
            return new StubSubjectService();
        }

        @Bean
        public PollsApiErrorHandler pollsApiErrorHandler() {
            return new PollsApiErrorHandler();
        }
    }

    @AfterEach
    public void afterTest() {
        stubPollRepository.deleteAll();
        stubSubjectService.deleteAllSubjects();
    }

    @Test
    public void testGetPollsNoPolls() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PollResponse.class).hasSize(0);
    }

    @Test
    public void testGetPollsSomePolls() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll1", startTime, endTime, 1L));
        service.createPoll(Poll.of(2L, "poll2", startTime, endTime, 1L));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PollResponse.class)
                        .contains(PollResponse.of(1L, "poll1", endTime),
                                PollResponse.of(2L, "poll2", endTime));
    }

    @Test
    public void testAddPollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(1));

        PollResponse pollResponse = webTestClient.post()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(PollCreate.of("poll", endDate)))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/subjects/1/polls/1")
                .returnResult(PollResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(1, pollResponse.getId());
        assertEquals("poll", pollResponse.getName());
        assertEquals(endDate, pollResponse.getEndDate());

        Optional<Poll> optional = service.getPollByIdAndSubjectId(1L, 1L);
        assertTrue(optional.isPresent());
        Poll poll = optional.get();
        assertEquals(1L, poll.getId());
        assertEquals("poll", poll.getName());
        assertEquals(endDate, poll.getEndDate());
    }

    @Test
    public void testAddPollNoEndDate() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        PollResponse pollResponse = webTestClient.post()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(PollCreate.of("poll", null)))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/subjects/1/polls/1")
                .returnResult(PollResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(1, pollResponse.getId());
        assertEquals("poll", pollResponse.getName());
        assertFalse(pollResponse.getEndDate().isAfter(
                OffsetDateTime.now().plus(Duration.ofMinutes(1))));

        Optional<Poll> optional = service.getPollByIdAndSubjectId(1L, 1L);
        assertTrue(optional.isPresent());
        Poll poll = optional.get();
        assertEquals(1L, poll.getId());
        assertEquals("poll", poll.getName());
        assertFalse(poll.getEndDate().isAfter(OffsetDateTime.now().plus(Duration.ofMinutes(1))));
    }

    @Test
    public void testAddPollWrongFormat() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).minus(Duration.ofHours(1));

        webTestClient.post()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(PollCreate.of("poll", endDate)))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void testUpdatePollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(1));

        service.createPoll(Poll.of(1L, "poll", startTime, endDate, 1L));

        PollResponse pollResponse = webTestClient.put()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(PollUpdate.of(1L, "poll updated", endDate)))
                .exchange()
                .expectStatus().isOk()
                .returnResult(PollResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(1L, pollResponse.getId());
        assertEquals("poll updated", pollResponse.getName());
        assertEquals(endDate, pollResponse.getEndDate());

        Optional<Poll> optional = service.getPollByIdAndSubjectId(1L, 1L);
        assertTrue(optional.isPresent());
        Poll poll = optional.get();
        assertEquals(1L, poll.getId());
        assertEquals("poll updated", poll.getName());
        assertEquals(endDate, poll.getEndDate());
    }

    @Test
    public void testUpdatePollWrongFormat() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).minus(Duration.ofHours(1));

        service.createPoll(Poll.of(1L, "poll", startTime, endDate, 1L));

        webTestClient.put()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(PollUpdate.of(1L, "poll", startTime.minus(Duration.ofDays(1)))))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void testUpdatePollNotFound() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime endDate = OffsetDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(1));

        webTestClient.put()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(PollUpdate.of(1L, "poll", endDate)))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testGetPollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        PollResponse pollResponse = webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PollResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(1L, pollResponse.getId());
        assertEquals("poll", pollResponse.getName());
        assertEquals(endTime, pollResponse.getEndDate());
    }

    @Test
    public void testGetPollNotFound() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testDeletePollSuccess() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        webTestClient.delete()
                .uri(URI.create("/subjects/1/polls/1"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void testDeletePollNotFound() throws Exception {
        stubSubjectService.createSubject(Subject.of(1L, "subject"));

        webTestClient.delete()
                .uri(URI.create("/subjects/1/polls/1"))
                .exchange()
                .expectStatus().isNotFound();
    }
}
