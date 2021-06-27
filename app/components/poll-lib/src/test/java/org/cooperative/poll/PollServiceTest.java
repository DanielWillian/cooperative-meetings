package org.cooperative.poll;

import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.exception.PollValidationException;
import org.cooperative.poll.exception.Validation;
import org.cooperative.poll.jpa.PollRepository;
import org.cooperative.subject.Subject;
import org.cooperative.subject.StubSubjectService;
import org.cooperative.subject.SubjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PollServiceTest {

    PollRepository pollRepository = new StubPollRepository();
    StubSubjectService subjectService = new StubSubjectService();
    PollService pollService = new PollServiceDefault(pollRepository, subjectService);

    @BeforeEach
    public void beforeEach() {
        subjectService.deleteAllSubjects();
        pollRepository.deleteAll();
    }

    @Test
    public void testCreatePollSuccess() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        Poll returnedPoll = pollService.createPoll(poll);
        Poll expectedPoll = poll.withId(returnedPoll.getId());
        assertEquals(expectedPoll, returnedPoll);

        Optional<org.cooperative.poll.jpa.Poll> optional = pollRepository.findById(expectedPoll.getId());
        assertFalse(optional.isEmpty());
        assertEquals(mapToJpa(expectedPoll), optional.get());
    }

    @Test
    public void testCreatePollSubjectNotFound() {
        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        assertThrows(SubjectNotFoundException.class, () -> pollService.createPoll(poll));
    }

    @Test
    public void testCreatePollMissingStartDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime endTime = OffsetDateTime.now().plus(Duration.ofMinutes(1));
        Poll poll = Poll.of(null, "poll", null, endTime, 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.MISSING_START_DATE));
    }

    @Test
    public void testCreatePollMissingEndDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime, null, 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.MISSING_END_DATE));
    }

    @Test
    public void testCreatePollNameTooLong() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(1L, " ".repeat(201), startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.NAME_TOO_LONG));
    }

    @Test
    public void testCreatePollEndDateEarlierThanStartDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(null, "poll", startTime,
                startTime.minus(Duration.ofMinutes(1)), 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.createPoll(poll));

        assertThat(exception.getValidations(), contains(Validation.END_DATE_EARLIER_THAN_START_DATE));
    }

    @Test
    public void testGetPollBySubjectIdNoPolls() {
        subjectService.createSubject(Subject.of(1L, "subject1"));
        subjectService.createSubject(Subject.of(2L, "subject2"));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                OffsetDateTime.now(), OffsetDateTime.now().plus(Duration.ofMinutes(1)),
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        List<Poll> polls = pollService.getPollBySubjectId(2L)
                .collect(Collectors.toList());

        assertTrue(polls.isEmpty());
    }

    @Test
    public void testGetPollBySubjectIdSomePolls() {
        subjectService.createSubject(Subject.of(1L, "subject1"));
        subjectService.createSubject(Subject.of(2L, "subject2"));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 2L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 3L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(2L, null)));

        List<Poll> polls = pollService.getPollBySubjectId(1L)
                .collect(Collectors.toList());

        assertEquals(2, polls.size());
        assertThat(polls, contains(Poll.of(1L, "poll", startTime, endTime, 1L),
                Poll.of(2L, "poll", startTime, endTime, 1L)));
    }

    @Test
    public void testGetPollBySubjectIdSubjectNotFound() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                OffsetDateTime.now(), OffsetDateTime.now().plus(Duration.ofMinutes(1)),
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        List<Poll> polls = pollService.getPollBySubjectId(2L)
                .collect(Collectors.toList());

        assertTrue(polls.isEmpty());
    }

    @Test
    public void testGetPollByIdAndSubjectIdNoPoll() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                OffsetDateTime.now(), OffsetDateTime.now().plus(Duration.ofMinutes(1)),
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        Optional<Poll> optional = pollService.getPollByIdAndSubjectId(2L, 1L);

        assertTrue(optional.isEmpty());
    }

    @Test
    public void testGetPollByIdAndSubjectId() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        Optional<Poll> optional = pollService.getPollByIdAndSubjectId(1L, 1L);

        assertFalse(optional.isEmpty());
        assertEquals(Poll.of(1L, "poll", startTime, endTime, 1L), optional.get());
    }

    @Test
    public void testGetPollByIdAndSubjectIdSubjectNotFound() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                OffsetDateTime.now(), OffsetDateTime.now().plus(Duration.ofMinutes(1)),
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        Optional<Poll> optional = pollService.getPollByIdAndSubjectId(2L, 2L);

        assertTrue(optional.isEmpty());
    }

    @Test
    public void testGetPollByNameAndSubjectIdNoPoll() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                OffsetDateTime.now(), OffsetDateTime.now().plus(Duration.ofMinutes(1)),
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        List<Poll> optional = pollService.getPollByNameAndSubjectId("poll name", 1L)
                .collect(Collectors.toList());

        assertTrue(optional.isEmpty());
    }

    @Test
    public void testGetPollByNameAndSubjectIdSomePools() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime,
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 2L, "poll",
                startTime, endTime,
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        List<Poll> polls = pollService.getPollByNameAndSubjectId("poll", 1L)
                .collect(Collectors.toList());

        assertEquals(2, polls.size());
        assertThat(polls, contains(Poll.of(1L, "poll", startTime, endTime, 1L),
                Poll.of(2L, "poll", startTime, endTime, 1L)));
    }

    @Test
    public void testGetPollByNameAndSubjectIdSubjectNotFound() {
        subjectService.createSubject(Subject.of(1L, "subject1"));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                OffsetDateTime.now(), OffsetDateTime.now().plus(Duration.ofMinutes(1)),
                org.cooperative.subject.jpa.Subject.of(1L, null)));

        List<Poll> optional = pollService.getPollByNameAndSubjectId("poll", 2L)
                .collect(Collectors.toList());

        assertTrue(optional.isEmpty());
    }

    @Test
    public void testUpdatePollSuccess() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        Poll poll = Poll.of(1L, "poll updated", startTime, endTime, 1L);
        Poll returnedPoll = pollService.updatePoll(poll);
        assertEquals(poll, returnedPoll);

        Optional<org.cooperative.poll.jpa.Poll> optional = pollRepository.findById(poll.getId());
        assertFalse(optional.isEmpty());
        assertEquals(mapToJpa(poll), optional.get());
    }

    @Test
    public void testUpdatePollSubjectNotFound() {
        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(1L, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        assertThrows(SubjectNotFoundException.class, () -> pollService.updatePoll(poll));
    }

    @Test
    public void testUpdatePollPollNotFound() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        Poll poll = Poll.of(1L, "poll", startTime,
                startTime.plus(Duration.ofMinutes(1)), 1L);
        assertThrows(PollNotFoundException.class, () -> pollService.updatePoll(poll));
    }

    @Test
    public void testUpdatePollMissingStartDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        Poll poll = Poll.of(1L, "poll", null, endTime, 1L);
        Poll returnedPoll = pollService.updatePoll(poll);
        Poll expectedPoll = poll.withStartDate(returnedPoll.getStartDate());
        assertEquals(expectedPoll, returnedPoll);

        Optional<org.cooperative.poll.jpa.Poll> optional = pollRepository.findById(expectedPoll.getId());
        assertFalse(optional.isEmpty());
        assertEquals(mapToJpa(expectedPoll), optional.get());
    }

    @Test
    public void testUpdatePollMissingEndDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        Poll poll = Poll.of(1L, "poll", startTime, null, 1L);
        Poll returnedPoll = pollService.updatePoll(poll);
        Poll expectedPoll = poll.withStartDate(returnedPoll.getStartDate());
        assertEquals(expectedPoll, returnedPoll);

        Optional<org.cooperative.poll.jpa.Poll> optional = pollRepository.findById(expectedPoll.getId());
        assertFalse(optional.isEmpty());
        assertEquals(mapToJpa(expectedPoll), optional.get());
    }

    @Test
    public void testUpdatePollNameTooLong() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        Poll poll = Poll.of(1L, " ".repeat(201), startTime, endTime, 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.updatePoll(poll));

        assertThat(exception.getValidations(), contains(Validation.NAME_TOO_LONG));
    }

    @Test
    public void testUpdatePollEndDateEarlierThanStartDate() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        Poll poll = Poll.of(1L, "poll", startTime,
                startTime.minus(Duration.ofMinutes(1)), 1L);
        PollValidationException exception = assertThrows(
                PollValidationException.class, () -> pollService.updatePoll(poll));

        assertThat(exception.getValidations(), contains(Validation.END_DATE_EARLIER_THAN_START_DATE));
    }

    @Test
    public void testDeletePollSuccess() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);

        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = startTime.plus(Duration.ofMinutes(1));

        pollRepository.save(org.cooperative.poll.jpa.Poll.of( 1L, "poll",
                startTime, endTime, org.cooperative.subject.jpa.Subject.of(1L, null)));

        pollService.deletePollByIdAndSubjectId(1L, 1L);

        Optional<org.cooperative.poll.jpa.Poll> optional = pollRepository.findById(1L);
        assertTrue(optional.isEmpty());
    }

    @Test
    public void testDeletePollSubjectNotFound() {
        assertThrows(SubjectNotFoundException.class, () -> pollService.deletePollByIdAndSubjectId(1L, 1L));
    }

    @Test
    public void testDeletePollPollNotFound() {
        Subject subject = Subject.of(1L, "subject");
        subjectService.createSubject(subject);
        assertThrows(PollNotFoundException.class, () -> pollService.deletePollByIdAndSubjectId(1L, 1L));
    }

    private org.cooperative.poll.jpa.Poll mapToJpa(Poll poll) {
        return org.cooperative.poll.jpa.Poll.builder()
                .id(poll.getId())
                .name(poll.getName())
                .startDate(poll.getStartDate())
                .endDate(poll.getEndDate())
                .subject(org.cooperative.subject.jpa.Subject.of(poll.getSubjectId(), null))
                .build();
    }
}
