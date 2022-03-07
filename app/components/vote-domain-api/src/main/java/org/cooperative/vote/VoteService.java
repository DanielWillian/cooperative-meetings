package org.cooperative.vote;

import org.cooperative.vote.exception.VoteAlreadyExistsException;
import org.cooperative.vote.exception.VoteNotFoundException;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface VoteService {
    Vote createVote(Vote vote) throws VoteAlreadyExistsException;
    Stream<Vote> getVoteBySubjectIdPollId(long subjectId, long pollId) throws VoteNotFoundException;
    Optional<Vote> getVoteBySubjectIdPollIdVoter(long subjectId, long pollId, UUID voter)
            throws VoteNotFoundException;
}
