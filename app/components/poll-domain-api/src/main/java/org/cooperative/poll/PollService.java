package org.cooperative.poll;

import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.exception.PollValidationException;
import org.cooperative.subject.SubjectNotFoundException;

import java.util.Optional;
import java.util.stream.Stream;

public interface PollService {
    Poll createPoll(Poll poll) throws SubjectNotFoundException, PollValidationException;
    Stream<Poll> getPollBySubjectId(long subjectId);
    Optional<Poll> getPollByIdAndSubjectId(long id, long subjectId) throws SubjectNotFoundException;
    Stream<Poll> getPollByNameAndSubjectId(String name, long subjectId)
            throws SubjectNotFoundException;
    Poll updatePoll(Poll poll) throws SubjectNotFoundException,
            PollValidationException, PollNotFoundException;
    void deletePollByIdAndSubjectId(long id, long subjectId)
            throws SubjectNotFoundException, PollNotFoundException;
}
