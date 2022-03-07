package org.cooperative.vote.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepositoryJpa extends CrudRepository<Vote, Long> {
    List<Vote> findByPoll_Subject_IdAndPoll_Id(long subjectId, long pollId);
    Optional<Vote> findByPoll_Subject_IdAndPoll_IdAndVoter(long subjectId, long pollId, UUID voter);

    @Query("SELECT v.agree as agree, COUNT(v.agree) as count FROM Vote v " +
            "WHERE v.poll.subject.id = :subjectId AND v.poll.id = :pollId " +
            "GROUP BY v.agree")
    List<VoteCount> countVotes(@Param("subjectId") long subjectId, @Param("pollId") long pollId);
}
