package org.cooperative.vote;

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
import org.cooperative.vote.api.VoteApiController;
import org.cooperative.vote.api.VoteApiErrorHandler;
import org.cooperative.vote.api.VoteCreate;
import org.cooperative.vote.api.VoteResponse;
import org.cooperative.vote.infrastructure.VoteRepositoryImpl;
import org.cooperative.vote.jpa.VoteRepositoryJpa;
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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@WebFluxTest(VoteApiController.class)
class VoteApiTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private VoteService voteService;

    @Autowired
    private PollService pollService;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StubVoteRepositoryJpa stubVoteRepository;

    @Autowired
    private StubPollRepositoryJpa stubPollRepository;

    @Autowired
    private StubSubjectRepositoryJpa stubSubjectRepository;

    private final OffsetDateTime startTime = OffsetDateTime.now();
    private final OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

    @Configuration
    public static class TestConfig {
        @Bean
        public VoteApiController voteApiController(VoteService service) {
            return new VoteApiController(service);
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
        public VoteApiErrorHandler voteApiErrorHandler() {
            return new VoteApiErrorHandler();
        }
    }

    @AfterEach
    void afterTest() {
        stubPollRepository.deleteAll();
        stubSubjectRepository.deleteAll();
        stubVoteRepository.deleteAll();
    }

    @Test
    void testGetVotesNoVotes() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1/votes"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VoteResponse.class).hasSize(0);
    }

    @Test
    void testGetVotesSomeVotes() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));
        Vote vote = Vote.builder()
                .voter(UUID.fromString("a7845af1-0117-4135-9912-a95f4db0b016"))
                .agree(true)
                .voteDate(startTime.plus(Duration.ofSeconds(10)))
                .subjectId(1L)
                .pollId(1L)
                .build();
        voteRepository.createVote(vote);

        VoteResponse voteResponse = webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1/votes"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(VoteResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(UUID.fromString("a7845af1-0117-4135-9912-a95f4db0b016"), voteResponse.getVoter());
        assertTrue(voteResponse.isAgree());
        assertTrue(startTime.plus(Duration.ofSeconds(10)).isEqual(voteResponse.getVoteDate()));
    }


    @Test
    void testAddVoteSuccess() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        UUID uuid = UUID.fromString("cfa91984-9824-4552-823d-662d29abd14e");

        VoteResponse voteResponse = webTestClient.post()
                .uri(URI.create("/subjects/1/polls/1/votes"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(VoteCreate.of(uuid, true)))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/subjects/1/polls/1/votes/cfa91984-9824-4552-823d-662d29abd14e")
                .returnResult(VoteResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(uuid, voteResponse.getVoter());
        assertTrue(voteResponse.isAgree());
        assertTrue(startTime.isBefore(voteResponse.getVoteDate()) &&
                endTime.isAfter(voteResponse.getVoteDate()));
    }

    @Test
    void testAddVoteTooLate() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime.minus(Duration.ofMinutes(1)), startTime, 1L));

        UUID uuid = UUID.fromString("ce31a8ca-56e3-4c08-b584-e28ba0ca1633");

        webTestClient.post()
                .uri(URI.create("/subjects/1/polls/1/votes"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(VoteCreate.of(uuid, true)))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void testAddVoteAlreadyExists() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        UUID uuid = UUID.fromString("a7de6af5-91ea-4a34-a9d2-35694cb9596d");

        Vote vote = Vote.builder()
                .voter(uuid)
                .agree(true)
                .voteDate(startTime.plus(Duration.ofSeconds(10)))
                .subjectId(1L)
                .pollId(1L)
                .build();
        voteRepository.createVote(vote);

        webTestClient.post()
                .uri(URI.create("/subjects/1/polls/1/votes"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(VoteCreate.of(uuid, true)))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testGetVoteSuccess() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        UUID uuid = UUID.fromString("a7de6af5-91ea-4a34-a9d2-35694cb9596d");

        Vote vote = Vote.builder()
                .voter(uuid)
                .agree(true)
                .voteDate(startTime.plus(Duration.ofSeconds(10)))
                .subjectId(1L)
                .pollId(1L)
                .build();
        voteRepository.createVote(vote);

        VoteResponse voteResponse = webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1/votes/a7de6af5-91ea-4a34-a9d2-35694cb9596d"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(VoteResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals(uuid, voteResponse.getVoter());
        assertTrue(voteResponse.isAgree());
        assertTrue(startTime.plus(Duration.ofSeconds(10)).isEqual(voteResponse.getVoteDate()));
    }

    @Test
    void testGetVoteNotFound() {
        subjectService.createSubject(Subject.of(1L, "subject"));
        pollService.createPoll(Poll.of(1L, "poll", startTime, endTime, 1L));

        webTestClient.get()
                .uri(URI.create("/subjects/1/polls/1/votes/7b79b8c4-4086-4a71-a621-7689d33dcf7d"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
