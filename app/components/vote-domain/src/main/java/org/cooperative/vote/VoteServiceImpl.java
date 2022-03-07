package org.cooperative.vote;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.poll.Poll;
import org.cooperative.poll.PollService;
import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.vote.exception.PollAlreadyEndedException;
import org.cooperative.vote.exception.VoteAlreadyExistsException;
import org.cooperative.vote.exception.VoteNotFoundException;
import org.cooperative.vote.exception.VoteValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;
    private final PollService pollService;

    @Autowired
    public VoteServiceImpl(VoteRepository voteRepository, PollService pollService) {
        this.voteRepository = voteRepository;
        this.pollService = pollService;
    }

    @Override
    public Vote createVote(Vote vote) throws VoteAlreadyExistsException {
        log.trace("BEGIN - create vote: {}", vote);
        if (vote.getVoter() == null) throw new VoteValidationException();
        if (vote.getSubjectId() == null) throw new VoteValidationException();
        if (vote.getPollId() == null) throw new VoteValidationException();

        Poll poll = pollService.getPollByIdAndSubjectId(vote.getPollId(), vote.getSubjectId())
                .orElseThrow(PollNotFoundException::new);
        OffsetDateTime now = OffsetDateTime.now();
        if (poll.getEndDate().isBefore(now)) throw new PollAlreadyEndedException();

        if (voteRepository.getVoteBySubjectIdPollIdVoter(
                vote.getSubjectId(), vote.getPollId(), vote.getVoter()).isPresent()) {
            throw new VoteAlreadyExistsException();
        }

        Vote returnVote = voteRepository.createVote(vote.withVoteDate(now));

        log.trace("EXIT - create vote: {}", vote);
        return returnVote;
    }

    @Override
    public Stream<Vote> getVoteBySubjectIdPollId(long subjectId, long pollId) throws VoteNotFoundException {
        log.trace("BEGIN - get vote subjectId: {}, pollId: {}", subjectId, pollId);
        Stream<Vote> votes = voteRepository.getVoteBySubjectIdPollId(subjectId, pollId);
        log.trace("EXIT - get vote subjectId: {}, pollId: {}", subjectId, pollId);
        return votes;
    }

    @Override
    public Optional<Vote> getVoteBySubjectIdPollIdVoter(long subjectId, long pollId, UUID voter)
            throws VoteNotFoundException {
        log.trace("BEGIN - get vote subjectId: {}, pollId: {}, voter: {}", subjectId, pollId, voter);
        Optional<Vote> vote = voteRepository.getVoteBySubjectIdPollIdVoter(subjectId, pollId, voter);
        log.trace("EXIT - get vote subjectId: {}, pollId: {}, voter: {}", subjectId, pollId, voter);
        return vote;
    }
}
