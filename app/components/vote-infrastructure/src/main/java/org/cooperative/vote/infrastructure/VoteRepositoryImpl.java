package org.cooperative.vote.infrastructure;

import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.jpa.Poll;
import org.cooperative.poll.jpa.PollRepositoryJpa;
import org.cooperative.vote.Vote;
import org.cooperative.vote.VoteRepository;
import org.cooperative.vote.jpa.VoteRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class VoteRepositoryImpl implements VoteRepository {
    private final VoteRepositoryJpa voteRepository;
    private final PollRepositoryJpa pollRepository;

    @Autowired
    public VoteRepositoryImpl(VoteRepositoryJpa voteRepository, PollRepositoryJpa pollRepository) {
        this.voteRepository = voteRepository;
        this.pollRepository = pollRepository;
    }

    @Override
    public Vote createVote(Vote vote) {
        return toDomain(voteRepository.save(fromDomain(vote)));
    }

    @Override
    public Stream<Vote> getVoteBySubjectIdPollId(long subjectId, long pollId) {
        return voteRepository.findByPoll_Subject_IdAndPoll_Id(subjectId, pollId).stream()
                .map(this::toDomain);
    }

    @Override
    public Optional<Vote> getVoteBySubjectIdPollIdVoter(long subjectId, long pollId, UUID voter) {
        return voteRepository.findByPoll_Subject_IdAndPoll_IdAndVoter(subjectId, pollId, voter)
                .map(this::toDomain);
    }

    private Vote toDomain(org.cooperative.vote.jpa.Vote vote) {
        return Vote.builder()
                .voter(vote.getVoter())
                .agree(vote.isAgree())
                .voteDate(vote.getVoteDate())
                .subjectId(vote.getPoll().getSubject().getId())
                .pollId(vote.getPoll().getId())
                .build();
    }

    private org.cooperative.vote.jpa.Vote fromDomain(Vote vote) {
        Poll poll = pollRepository.findBySubject_IdAndId(vote.getSubjectId(), vote.getPollId())
                .orElseThrow(PollNotFoundException::new);
        return org.cooperative.vote.jpa.Vote.builder()
                .voter(vote.getVoter())
                .agree(vote.isAgree())
                .voteDate(vote.getVoteDate())
                .poll(poll)
                .build();
    }
}
