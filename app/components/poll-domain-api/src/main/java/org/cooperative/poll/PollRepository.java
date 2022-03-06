package org.cooperative.poll;

import java.util.Optional;
import java.util.stream.Stream;

public interface PollRepository {
    Poll save(Poll poll);
    Stream<Poll> getBySubjectId(long subjectId);
    Stream<Poll> getBySubjectIdAndPollName(long subjectId, String name);
    Optional<Poll> getBySubjectIdAndPollId(long subjectId, long pollId);
    void deleteBySubjectIdAndPollId(long subjectId, long pollId);
}
