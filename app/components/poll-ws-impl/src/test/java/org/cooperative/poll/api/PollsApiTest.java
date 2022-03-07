package org.cooperative.poll.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cooperative.poll.Poll;
import org.cooperative.poll.PollRepository;
import org.cooperative.poll.PollService;
import org.cooperative.poll.PollServiceDefault;
import org.cooperative.poll.infrastructure.PollRepositoryImpl;
import org.cooperative.poll.jpa.PollRepositoryJpa;
import org.cooperative.poll.jpa.StubPollRepositoryJpa;
import org.cooperative.subject.StubSubjectRepositoryJpa;
import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectRepository;
import org.cooperative.subject.SubjectService;
import org.cooperative.subject.SubjectServiceDefault;
import org.cooperative.subject.infrastructure.SubjectRepositoryImpl;
import org.cooperative.subject.jpa.SubjectRepositoryJpa;
import org.cooperative.vote.StubVoteRepositoryJpa;
import org.cooperative.vote.Vote;
import org.cooperative.vote.VoteRepository;
import org.cooperative.vote.VoteService;
import org.cooperative.vote.VoteServiceImpl;
import org.cooperative.vote.infrastructure.VoteRepositoryImpl;
import org.cooperative.vote.jpa.VoteRepositoryJpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    private VoteService voteService;

    @Autowired
    private StubPollRepositoryJpa stubPollRepository;

    @Autowired
    private StubSubjectRepositoryJpa stubSubjectRepository;

    @Autowired
    private StubVoteRepositoryJpa stubVoteRepositoryJpa;

    @Autowired
    private SubjectService SubjectService;

    private final String startTimeString = "2021-06-27T12:00:00Z";
    private final String endTimeString = "2021-06-27T13:00:00Z";
    private final OffsetDateTime startTime = OffsetDateTime.parse(startTimeString);
    private final OffsetDateTime endTime = OffsetDateTime.parse(endTimeString);

    @Configuration
    public static class TestConfig {
        @Bean
        public PollsApiController pollsApiController(PollService service, VoteService voteService) {
            return new PollsApiController(service, voteService);
        }

        @Bean
        public VoteService voteService(VoteRepository voteRepository, PollService pollService) {
            return new VoteServiceImpl(voteRepository, pollService);
        }

        @Bean
        public VoteRepository voteRepository(VoteRepositoryJpa voteRepositoryJpa, PollRepositoryJpa pollRepositoryJpa) {
            return new VoteRepositoryImpl(voteRepositoryJpa, pollRepositoryJpa);
        }

        @Bean
        public VoteRepositoryJpa voteRepositoryJpa() {
            return new StubVoteRepositoryJpa();
        }

        @Bean
        public PollService service(PollRepository repository, SubjectService service) {
            return new PollServiceDefault(repository, service);
        }

        @Bean
        public PollRepository pollRepository(PollRepositoryJpa pollRepositoryJpa,
                SubjectRepositoryJpa subjectRepositoryJpa) {
            return new PollRepositoryImpl(pollRepositoryJpa, subjectRepositoryJpa);
        }

        @Bean
        public PollRepositoryJpa pollRepositoryJpa() {
            return new StubPollRepositoryJpa();
        }

        @Bean
        public SubjectService subjectService(SubjectRepository repository) {
            return new SubjectServiceDefault(repository);
        }

        @Bean
        public SubjectRepository subjectRepository(SubjectRepositoryJpa subjectRepositoryJpa) {
            return new SubjectRepositoryImpl(subjectRepositoryJpa);
        }

        @Bean
        public SubjectRepositoryJpa subjectRepositoryJpa() {
            return new StubSubjectRepositoryJpa();
        }

        @Bean
        public PollsApiErrorHandler pollsApiErrorHandler() {
            return new PollsApiErrorHandler();
        }
    }

    @AfterEach
    public void afterTest() {
        stubPollRepository.deleteAll();
        stubSubjectRepository.deleteAll();
        stubVoteRepositoryJpa.deleteAll();
    }

    @Test
    public void testGetPollsNoPolls() throws Exception {
        SubjectService.createSubject(Subject.of(1L, "subject"));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PollResponse.class).hasSize(0);
    }

    @Test
    public void testGetPollsSomePolls() throws Exception {
        SubjectService.createSubject(Subject.of(1L, "subject"));
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
        SubjectService.createSubject(Subject.of(1L, "subject"));

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
        SubjectService.createSubject(Subject.of(1L, "subject"));

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
        SubjectService.createSubject(Subject.of(1L, "subject"));

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
        SubjectService.createSubject(Subject.of(1L, "subject"));
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
        SubjectService.createSubject(Subject.of(1L, "subject"));
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
        SubjectService.createSubject(Subject.of(1L, "subject"));
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
        SubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        PollVotesResponse pollResponse = webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PollVotesResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(1L, pollResponse.getId());
        assertEquals("poll", pollResponse.getName());
        assertEquals(endTime, pollResponse.getEndDate());
        assertEquals(0, pollResponse.getVotes().getAgree());
        assertEquals(0, pollResponse.getVotes().getDisagree());
    }

    @Test
    public void testGetPollSuccessWithVotes() throws Exception {
        SubjectService.createSubject(Subject.of(1L, "subject"));
        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));
        voteService.createVote(Vote.of(UUID.fromString("c3ebfe9d-eb65-4391-83d4-b9bf69c83672"), false, null, 1L, 1L));
        voteService.createVote(Vote.of(UUID.fromString("e6a3599c-e003-47d4-a63a-75bc6f530154"), true, null, 1L, 1L));
        voteService.createVote(Vote.of(UUID.fromString("c57117f9-0031-4f92-ae39-ff7198613ff4"), true, null, 1L, 1L));

        PollVotesResponse pollResponse = webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PollVotesResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(1L, pollResponse.getId());
        assertEquals("poll", pollResponse.getName());
        assertTrue(endTime.isEqual(pollResponse.getEndDate()));
        assertEquals(2, pollResponse.getVotes().getAgree());
        assertEquals(1, pollResponse.getVotes().getDisagree());
    }

    @Test
    public void testGetPollNotFound() throws Exception {
        SubjectService.createSubject(Subject.of(1L, "subject"));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void testDeletePollSuccess() throws Exception {
        SubjectService.createSubject(Subject.of(1L, "subject"));
        service.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        webTestClient.delete()
                .uri(URI.create("/subjects/1/polls/1"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void testDeletePollNotFound() throws Exception {
        SubjectService.createSubject(Subject.of(1L, "subject"));

        webTestClient.delete()
                .uri(URI.create("/subjects/1/polls/1"))
                .exchange()
                .expectStatus().isNotFound();
    }
}
