package org.cooperative.vote;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface VoteRepository {
    Vote createVote(Vote vote);
    Stream<Vote> getVoteBySubjectIdPollId(long subjectId, long pollId);
    Optional<Vote> getVoteBySubjectIdPollIdVoter(long subjectId, long pollId, UUID voteId);
}
