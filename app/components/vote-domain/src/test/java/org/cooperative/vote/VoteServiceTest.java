package org.cooperative.vote;

import org.cooperative.poll.Poll;
import org.cooperative.poll.PollService;
import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.vote.exception.VoteAlreadyExistsException;
import org.cooperative.vote.exception.VoteValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {
    @InjectMocks
    VoteServiceImpl voteService;

    @Mock
    VoteRepository voteRepository;

    @Mock
    PollService pollService;

    @Captor
    ArgumentCaptor<Vote> voteCaptor;

    OffsetDateTime startTime = OffsetDateTime.now();
    OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));
    UUID uuid = UUID.fromString("ecda5a8b-c169-44de-b2bd-d5046a8a13a9");

    @Test
    void testCreateVoteSuccess() {
        when(pollService.getPollByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(Poll.of(1L, "poll", startTime, endTime, 1L)));
        when(voteRepository.getVoteBySubjectIdPollIdVoter(1L, 1L, uuid))
                .thenReturn(Optional.empty());
        voteService.createVote(Vote.of(uuid, true, null, 1L, 1L));
        verify(voteRepository, times(1))
                .createVote(voteCaptor.capture());
        Vote vote = voteCaptor.getValue();
        assertEquals(uuid, vote.getVoter());
        assertTrue(vote.isAgree());
        assertFalse(vote.getVoteDate().isAfter(endTime));
    }

    @Test
    void testCreateVoteVoterNull() {
        Vote vote = Vote.of(null, true, null, 1L, 1L);
        assertThrows(VoteValidationException.class, () -> voteService.createVote(vote));
    }

    @Test
    void testCreateVoteSubjectIdNull() {
        Vote vote = Vote.of(uuid, true, null, null, 1L);
        assertThrows(VoteValidationException.class, () -> voteService.createVote(vote));
    }

    @Test
    void testCreateVotePollIdNull() {
        Vote vote = Vote.of(uuid, true, null, 1L, null);
        assertThrows(VoteValidationException.class, () -> voteService.createVote(vote));
    }

    @Test
    void testCreateVotePollNotFound() {
        when(pollService.getPollByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.empty());
        Vote vote = Vote.of(uuid, true, null, 1L, 1L);
        assertThrows(PollNotFoundException.class, () -> voteService.createVote(vote));
    }

    @Test
    void testCreateVotePollAlreadyExists() {
        when(pollService.getPollByIdAndSubjectId(1L, 1L))
                .thenReturn(Optional.of(Poll.of(1L, "poll", startTime, endTime, 1L)));
        when(voteRepository.getVoteBySubjectIdPollIdVoter(1L, 1L, uuid))
                .thenReturn(Optional.of(Vote.of(uuid, true, OffsetDateTime.now(), 1L, 1L)));
        Vote vote = Vote.of(uuid, true, null, 1L, 1L);
        assertThrows(VoteAlreadyExistsException.class, () -> voteService.createVote(vote));
    }

    @Test
    void testGetVoteBySubjectIdPollId() {
        voteService.getVoteBySubjectIdPollId(2L, 1L);
        verify(voteRepository, times(1))
                .getVoteBySubjectIdPollId(2L, 1L);
    }

    @Test
    void testGetVoteBySubjectIdPollIdVoter() {
        voteService.getVoteBySubjectIdPollIdVoter(2L, 1L, uuid);
        verify(voteRepository, times(1))
                .getVoteBySubjectIdPollIdVoter(2L, 1L, uuid);
    }

    @Test
    void testGetVoteCountForPoll() {
        voteService.getVoteCountForPoll(2L, 1L);
        verify(voteRepository, times(1))
                .getVoteCountForPoll(2L, 1L);
    }
}
