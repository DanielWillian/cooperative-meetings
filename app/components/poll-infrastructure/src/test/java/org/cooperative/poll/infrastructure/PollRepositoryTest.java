package org.cooperative.poll.infrastructure;

import org.cooperative.poll.jpa.Poll;
import org.cooperative.poll.jpa.PollRepositoryJpa;
import org.cooperative.subject.jpa.Subject;
import org.cooperative.subject.jpa.SubjectRepositoryJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollRepositoryTest {
    @InjectMocks
    PollRepositoryImpl pollRepository;

    @Mock
    PollRepositoryJpa pollRepositoryJpa;

    @Mock
    SubjectRepositoryJpa subjectRepositoryJpa;

    OffsetDateTime startDate = OffsetDateTime.now();
    OffsetDateTime endDate = startDate.plus(Duration.ofMinutes(1));

    @Test
    void testSave() {
        when(subjectRepositoryJpa.findById(1L))
                .thenReturn(Optional.of(Subject.of(1L, "subject")));
        when(pollRepositoryJpa.save(Poll.of(1, "poll", startDate, endDate,
                Subject.of(1L, "subject"))))
                .thenReturn(Poll.of(1, "poll", startDate, endDate,
                        Subject.of(1L, "subject")));
        org.cooperative.poll.Poll poll = pollRepository.save(
                org.cooperative.poll.Poll.of(1L, "poll", startDate, endDate, 1L));
        assertEquals(org.cooperative.poll.Poll.of(1L, "poll", startDate, endDate, 1L), poll);
    }

    @Test
    void testGetBySubjectId() {
        when(pollRepositoryJpa.findBySubject_Id(1L))
                .thenReturn(List.of(Poll.of(1, "poll", startDate, endDate, Subject.of(1L, "subject"))));
        List<org.cooperative.poll.Poll> polls = pollRepository.getBySubjectId(1).collect(Collectors.toList());
        assertEquals(org.cooperative.poll.Poll.of(1L, "poll", startDate, endDate, 1L), polls.get(0));
    }

    @Test
    void testGetBySubjectIdAndPollName() {
        when(pollRepositoryJpa.findBySubject_IdAndName(1L, "poll"))
                .thenReturn(List.of(Poll.of(1, "poll", startDate, endDate, Subject.of(1L, "subject"))));
        List<org.cooperative.poll.Poll> polls = pollRepository.getBySubjectIdAndPollName(1, "poll")
                .collect(Collectors.toList());
        assertEquals(org.cooperative.poll.Poll.of(1L, "poll", startDate, endDate, 1L), polls.get(0));
    }

    @Test
    void testGetBySubjectIdAndPollId() {
        when(pollRepositoryJpa.findBySubject_IdAndId(1L, 1L))
                .thenReturn(Optional.of(Poll.of(1, "poll", startDate, endDate, Subject.of(1L, "subject"))));
        Optional<org.cooperative.poll.Poll> polls = pollRepository.getBySubjectIdAndPollId(1, 1);
        assertEquals(org.cooperative.poll.Poll.of(1L, "poll", startDate, endDate, 1L), polls.get());
    }

    @Test
    void testDeleteBySubjectIdAndPollId() {
        pollRepository.deleteBySubjectIdAndPollId(1, 1);
        verify(pollRepositoryJpa, times(1))
                .deleteBySubject_IdAndId(1, 1);
    }
}
