package org.cooperative.poll;

import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.exception.PollValidationException;
import org.cooperative.poll.exception.Validation;
import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.SubjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollServiceTest {

    @InjectMocks
    PollServiceDefault pollService;

    @Mock
    PollRepository pollRepository;

    @Mock
    SubjectService subjectService;

    @Test
    void testCreatePollSuccess() {
        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);

        pollService.createPoll(poll);
        verify(pollRepository, times(1))
                .save(Poll.of(null, "poll", startTime, startTime.plus(Duration.ofMinutes(1)), 1L));
    }

    @Test
    void testCreatePollSubjectNotFound() {
        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        when(pollRepository.save(poll))
                .thenThrow(SubjectNotFoundException.class);

        assertThrows(SubjectNotFoundException.class, () -> pollService.createPoll(poll));
    }

    @Test
    void testCreatePollMissingStartDate() {
        OffsetDateTime endTime = OffsetDateTime.now().plus(Duration.ofMinutes(1));
        Poll poll = Poll.of(null, "poll", null, endTime, 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.MISSING_START_DATE));
    }

    @Test
    void testCreatePollMissingEndDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime, null, 1L);

        pollService.createPoll(poll);

        verify(pollRepository, times(1))
                .save(poll.withEndDate(startTime.plus(Duration.ofMinutes(1))));
    }

    @Test
    void testCreatePollNameTooLong() {
        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(1L, " ".repeat(201), startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.NAME_TOO_LONG));
    }

    @Test
    void testCreatePollEndDateEarlierThanStartDate() {
        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime,
                startTime.minus(Duration.ofMinutes(1)), 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.END_DATE_EARLIER_THAN_START_DATE));
    }

    @Test
    void testGetPollBySubjectId() {
        pollService.getPollBySubjectId(2L);
        verify(pollRepository, times(1))
                .getBySubjectId(2);
    }

    @Test
    void testGetPollByIdAndSubjectId() {
        pollService.getPollByIdAndSubjectId(2L, 1L);
        verify(pollRepository, times(1))
                .getBySubjectIdAndPollId(1L, 2L);
    }

    @Test
    public void testGetPollByNameAndSubjectId() {
        pollService.getPollByNameAndSubjectId("poll name", 1L);
        verify(pollRepository, times(1))
                .getBySubjectIdAndPollName(1L, "poll name");
    }

    @Test
    public void testUpdatePollSuccess() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));
        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.of(Poll.of( 1L, "poll", startTime, endTime, 1L)));

        Poll poll = Poll.of(1L, "poll updated", startTime, endTime, 1L);
        pollService.updatePoll(poll);
        verify(pollRepository, times(1))
                .save(Poll.of(1L, "poll updated", startTime, endTime, 1L));
    }

    @Test
    public void testUpdatePollSubjectNotFound() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.empty());

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(1L, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        assertThrows(SubjectNotFoundException.class, () -> pollService.updatePoll(poll));
    }

    @Test
    public void testUpdatePollPollNotFound() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));

        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.empty());

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(1L, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        assertThrows(PollNotFoundException.class, () -> pollService.updatePoll(poll));
    }

    @Test
    public void testUpdatePollMissingStartDate() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.of(Poll.of( 1L, "poll", startTime, endTime, 1L)));

        Poll poll = Poll.of(1L, "poll", null, endTime, 1L);
        pollService.updatePoll(poll);
        verify(pollRepository, times(1))
                .save(Poll.of(1L, "poll", startTime, endTime, 1L));
    }

    @Test
    public void testUpdatePollMissingEndDate() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.of(Poll.of( 1L, "poll", startTime, endTime, 1L)));

        Poll poll = Poll.of(1L, "poll", startTime, null, 1L);
        pollService.updatePoll(poll);
        verify(pollRepository, times(1))
                .save(Poll.of(1L, "poll", startTime, endTime, 1L));
    }

    @Test
    public void testUpdatePollNameTooLong() {
        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));
        Poll poll = Poll.of(1L, " ".repeat(201), startTime, endTime, 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.updatePoll(poll));

        assertThat(exception.getValidations(), contains(Validation.NAME_TOO_LONG));
    }

    @Test
    public void testUpdatePollEndDateEarlierThanStartDate() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.of(Poll.of( 1L, "poll", startTime, endTime, 1L)));

        Poll poll = Poll.of(1L, "poll", startTime,
                startTime.minus(Duration.ofMinutes(1)), 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.updatePoll(poll));

        assertThat(exception.getValidations(), contains(Validation.END_DATE_EARLIER_THAN_START_DATE));
    }

    @Test
    public void testDeletePollSuccess() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.of(Poll.of( 1L, "poll", startTime, endTime, 1L)));

        pollService.deletePollByIdAndSubjectId(1L, 1L);
        verify(pollRepository, times(1))
                .deleteBySubjectIdAndPollId(1L, 1L);
    }

    @Test
    public void testDeletePollSubjectNotFound() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.empty());
        assertThrows(SubjectNotFoundException.class, () -> pollService.deletePollByIdAndSubjectId(1L, 1L));
    }

    @Test
    public void testDeletePollPollNotFound() {
        when(subjectService.getSubjectById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));
        when(pollRepository.getBySubjectIdAndPollId(1L, 1L))
                .thenReturn(Optional.empty());
        assertThrows(PollNotFoundException.class, () -> pollService.deletePollByIdAndSubjectId(1L, 1L));
    }
}
