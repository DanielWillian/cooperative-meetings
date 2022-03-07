package org.cooperative.vote.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.vote.Vote;
import org.cooperative.vote.VoteService;
import org.cooperative.vote.exception.VoteNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/subjects/{subjectId}/polls/{pollId}/votes")
public class VoteApiController {
    private final VoteService voteService;

    @Autowired
    public VoteApiController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping
    public Flux<VoteResponse> getVotes(@PathVariable("subjectId") long subjectId,
            @PathVariable("pollId") long pollId) {
        log.info("Received get all votes, subjectId: {}, pollId: {}", subjectId, pollId);
        return Flux.defer(() -> getVotesFromService(subjectId, pollId))
                .map(VoteResponse::fromDomain);
    }

    private Flux<Vote> getVotesFromService(long subjectId, long pollId) {
        log.trace("ENTRY - get votes from service, subjectId: {}, pollId: {}", subjectId, pollId);
        Flux<Vote> votes = Flux.fromStream(() -> voteService.getVoteBySubjectIdPollId(subjectId, pollId))
                .subscribeOn(Schedulers.boundedElastic());
        log.trace("EXIT - get votes from service, subjectId: {}, pollId: {}", subjectId, pollId);
        return votes;
    }

    @PostMapping
    public Mono<ResponseEntity<VoteResponse>> addVote(@PathVariable("subjectId") long subjectId,
            @PathVariable("pollId") long pollId,
            @RequestBody VoteCreate voteCreate) {
        log.info("Received add vote, subjectId: {}, pollId: {}, voteCreate: {}", subjectId, pollId, voteCreate);
        return Mono.defer(() -> addVoteFromService(subjectId, pollId, voteCreate))
                .map(VoteResponse::fromDomain)
                .map(v -> ResponseEntity.created(
                                URI.create("/subjects/" + subjectId + "/polls/" + pollId + "/votes/" + v.getVoter()))
                        .body(v));
    }

    private Mono<Vote> addVoteFromService(long subjectId, long pollId, VoteCreate voteCreate) {
        log.trace("ENTRY - add vote from service, subjectId: {}, pollId: {}, voteCreate: {}",
                subjectId, pollId, voteCreate);
        Vote vote = Vote.builder()
                .voter(voteCreate.getVoter())
                .agree(voteCreate.isAgree())
                .subjectId(subjectId)
                .pollId(pollId)
                .build();
        Mono<Vote> mono = Mono.just(vote)
                .flatMap(v -> Mono.fromCallable(() -> voteService.createVote(v))
                        .subscribeOn(Schedulers.boundedElastic()));
        log.trace("EXIT - add vote from service, subjectId: {}, pollId: {}, voteCreate: {}",
                subjectId, pollId, voteCreate);
        return mono;
    }

    @GetMapping("/{voter}")
    public Mono<VoteResponse> getVote(@PathVariable("subjectId") long subjectId,
            @PathVariable("pollId") long pollId,
            @PathVariable("voter") UUID voter) {
        log.info("Received get vote, subjectId: {}, pollId: {}, voter: {}", subjectId, pollId, voter);
        return Mono.defer(() -> getVoteFromService(subjectId, pollId, voter))
                .map(VoteResponse::fromDomain);
    }

    private Mono<Vote> getVoteFromService(long subjectId, long pollId, UUID voter) {
        log.trace("ENTRY - get vote from service, subjectId: {}, pollId: {}, voter: {}",
                subjectId, pollId, voter);
        Mono<Vote> mono = Mono.fromCallable(() -> voteService.getVoteBySubjectIdPollIdVoter(subjectId, pollId, voter)
                        .orElseThrow(VoteNotFoundException::new))
                .subscribeOn(Schedulers.boundedElastic());
        log.trace("EXIT - get vote from service, subjectId: {}, pollId: {}, voter: {}",
                subjectId, pollId, voter);
        return mono;
    }
}
