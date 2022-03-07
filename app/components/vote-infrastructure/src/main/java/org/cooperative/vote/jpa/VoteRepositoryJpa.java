package org.cooperative.vote.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepositoryJpa extends CrudRepository<Vote, Long> {
    List<Vote> findByPoll_Subject_IdAndPoll_Id(long subjectId, long pollId);
    Optional<Vote> findByPoll_Subject_IdAndPoll_IdAndVoter(long subjectId, long pollId, UUID voter);
}
