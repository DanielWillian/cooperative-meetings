package org.cooperative.poll.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.poll.Poll;
import org.cooperative.poll.PollService;
import org.cooperative.poll.exception.PollNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/subjects/{subjectId}/polls")
public class PollsApiController {

    private final PollService pollService;

    @Autowired
    public PollsApiController(PollService pollService) {
        this.pollService = pollService;
    }

    @GetMapping
    public Flux<PollResponse> getPolls(@PathVariable("subjectId") long subjectId) {
        log.info("Received get all polls from subject: {}", subjectId);
        return Flux.defer(() -> getPollsFromService(subjectId))
                .map(PollResponse::fromDomain);
    }

    private Flux<Poll> getPollsFromService(long subjectId) {
        log.trace("ENTRY - get polls from service, subjectId: {}", subjectId);
        Flux<Poll> polls = Flux.fromStream(() -> pollService.getPollBySubjectId(subjectId))
                .subscribeOn(Schedulers.boundedElastic());
        log.trace("EXIT - get polls from service, subjectId: {}", subjectId);
        return polls;
    }

    @PostMapping
    public Mono<ResponseEntity<PollResponse>> addPoll(@PathVariable("subjectId") long subjectId,
            @RequestBody PollCreate pollCreate) {
        log.info("Received add poll for subject: {}, pollCreate: {}", subjectId, pollCreate);
        return Mono.defer(() -> addPollFromService(subjectId, pollCreate))
                .map(PollResponse::fromDomain)
                .map(s -> ResponseEntity.created(URI.create("/subjects/" + subjectId + "/polls/" + s.getId())).body(s));
    }

    private Mono<Poll> addPollFromService(long subjectId, PollCreate pollCreate) {
        log.trace("ENTRY - add poll from service, subjectId: {}, pollCreate: {}", subjectId, pollCreate);
        Mono<Poll> poll = Mono.just(PollCreate.toDomain(pollCreate, subjectId))
                .flatMap(p -> Mono.fromCallable(() -> pollService.createPoll(p))
                        .subscribeOn(Schedulers.boundedElastic()));
        log.trace("EXIT - add poll from service, subjectId: {}, pollCreate: {}", subjectId, pollCreate);
        return poll;
    }

    @PutMapping
    public Mono<PollResponse> updatePoll(@PathVariable("subjectId") long subjectId,
            @RequestBody PollUpdate pollUpdate) {
        log.info("Received update poll for subject: {}, pollUpdate: {}", subjectId, pollUpdate);
        return Mono.defer(() -> updatePollFromService(subjectId, pollUpdate))
                .map(PollResponse::fromDomain);
    }

    private Mono<Poll> updatePollFromService(long subjectId, PollUpdate pollUpdate) {
        log.trace("ENTRY - update poll from service, subjectId: {}, pollUpdate: {}", subjectId, pollUpdate);
        Mono<Poll> poll = Mono.just(PollUpdate.toDomain(pollUpdate, subjectId))
                .flatMap(p -> Mono.fromCallable(() -> pollService.updatePoll(p))
                        .subscribeOn(Schedulers.boundedElastic()));
        log.trace("EXIT - update poll from service, subjectId: {}, pollUpdate: {}", subjectId, pollUpdate);
        return poll;
    }

    @GetMapping("/{pollId}")
    public Mono<PollResponse> getPollById(@PathVariable("subjectId") long subjectId,
            @PathVariable("pollId") long pollId) {
        log.info("Received get poll of subject: {}, poll: {}", subjectId, pollId);
        return Mono.defer(() -> getPollByIdFromService(subjectId, pollId))
                .map(PollResponse::fromDomain);
    }

    private Mono<Poll> getPollByIdFromService(long subjectId, long pollId) {
        log.trace("ENTRY - get poll by id from service, subjectId: {}, pollId: {}", subjectId, pollId);
        Mono<Poll> poll = Mono.fromCallable(() -> pollService.getPollByIdAndSubjectId(pollId, subjectId)
                        .orElseThrow(PollNotFoundException::new))
                .subscribeOn(Schedulers.boundedElastic());
        log.trace("EXIT - get poll by id from service, subjectId: {}, pollId: {}", subjectId, pollId);
        return poll;
    }

    @DeleteMapping("/{pollId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deletePollById(@PathVariable("subjectId") long subjectId,
            @PathVariable("pollId") long pollId) {
        log.info("Received delete poll of subject: {}, poll: {}", subjectId, pollId);
        return Mono.defer(() -> deletePollByIdFromService(subjectId, pollId));
    }

    private Mono<Void> deletePollByIdFromService(long subjectId, long pollId) {
        log.trace("ENTRY - delete poll by id from service, subjectId: {}, pollId: {}", subjectId, pollId);
        Mono<Void> poll = Mono.just(Poll.builder()
                        .subjectId(subjectId)
                        .id(pollId)
                        .build())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(p -> pollService.deletePollByIdAndSubjectId(p.getId(), p.getSubjectId()))
                .then();
        log.trace("EXIT - delete poll by id from service, subjectId: {}, pollId: {}", subjectId, pollId);
        return poll;
    }
}
