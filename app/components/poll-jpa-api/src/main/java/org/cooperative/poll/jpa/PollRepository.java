package org.cooperative.poll.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PollRepository extends CrudRepository<Poll, Long> {
    List<Poll> findBySubject_Id(long subjectId);
    Optional<Poll> findBySubject_IdAndId(long subjectId, long pollId);
    List<Poll> findBySubject_IdAndName(long subjectId, String name);
}
