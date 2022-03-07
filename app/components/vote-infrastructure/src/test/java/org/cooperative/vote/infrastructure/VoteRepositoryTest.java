package org.cooperative.vote.infrastructure;

import org.cooperative.poll.jpa.Poll;
import org.cooperative.poll.jpa.PollRepositoryJpa;
import org.cooperative.subject.jpa.Subject;
import org.cooperative.vote.Vote;
import org.cooperative.vote.jpa.VoteRepositoryJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteRepositoryTest {
    @InjectMocks
    VoteRepositoryImpl voteRepository;

    @Mock
    VoteRepositoryJpa voteRepositoryJpa;

    @Mock
    PollRepositoryJpa pollRepositoryJpa;

    OffsetDateTime startTime = OffsetDateTime.now();
    OffsetDateTime voteTime = startTime.plus(Duration.ofSeconds(10));
    OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));
    UUID uuid = UUID.fromString("3855acfe-d7c3-4358-9c79-a8b8cb3c2d08");

    @Test
    void testCreateVote() {
        when(pollRepositoryJpa.findBySubject_IdAndId(1L, 1L))
                .thenReturn(Optional.of(Poll.of(1L, "poll", startTime, endTime, Subject.of(1L, "subject"))));
        when(voteRepositoryJpa.save(org.cooperative.vote.jpa.Vote.of(null, uuid, false, voteTime,
                        Poll.of(1L, "poll", startTime, endTime, Subject.of(1L, "subject")))))
                .thenReturn(org.cooperative.vote.jpa.Vote.of(1L, uuid, false, voteTime,
                        Poll.of(1L, "poll", startTime, endTime, Subject.of(1L, "subject"))));
        Vote vote = voteRepository.createVote(Vote.of(uuid, false, voteTime, 1L, 1L));
        assertEquals(Vote.of(uuid, false, voteTime, 1L, 1L), vote);
    }

    @Test
    void testGetVoteBySubjectIdPollId() {
        voteRepository.getVoteBySubjectIdPollId(1L, 1L);
        verify(voteRepositoryJpa, times(1))
                .findByPoll_Subject_IdAndPoll_Id(1L, 1L);
    }

    @Test
    void testGetVoteBySubjectIdPollIdVoter() {
        voteRepository.getVoteBySubjectIdPollIdVoter(1L, 1L, uuid);
        verify(voteRepositoryJpa, times(1))
                .findByPoll_Subject_IdAndPoll_IdAndVoter(1L, 1L, uuid);
    }
}
