package org.cooperative.poll.infrastructure;

import org.cooperative.poll.Poll;
import org.cooperative.poll.PollRepository;
import org.cooperative.poll.jpa.PollRepositoryJpa;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.jpa.Subject;
import org.cooperative.subject.jpa.SubjectRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Transactional
public class PollRepositoryImpl implements PollRepository {
    private final PollRepositoryJpa pollRepositoryJpa;
    private final SubjectRepositoryJpa subjectRepositoryJpa;

    @Autowired
    public PollRepositoryImpl(PollRepositoryJpa pollRepositoryJpa, SubjectRepositoryJpa subjectRepositoryJpa) {
        this.pollRepositoryJpa = pollRepositoryJpa;
        this.subjectRepositoryJpa = subjectRepositoryJpa;
    }

    @Override
    public Poll save(Poll poll) {
        return mapFromJpa(pollRepositoryJpa.save(mapToJpa(poll)));
    }

    @Override
    public Stream<Poll> getBySubjectId(long subjectId) {
        return pollRepositoryJpa.findBySubject_Id(subjectId).stream()
                .map(this::mapFromJpa);
    }

    @Override
    public Stream<Poll> getBySubjectIdAndPollName(long subjectId, String name) {
        return pollRepositoryJpa.findBySubject_IdAndName(subjectId, name).stream()
                .map(this::mapFromJpa);
    }

    @Override
    public Optional<Poll> getBySubjectIdAndPollId(long subjectId, long pollId) {
        return pollRepositoryJpa.findBySubject_IdAndId(subjectId, pollId)
                .map(this::mapFromJpa);
    }

    @Override
    public void deleteBySubjectIdAndPollId(long subjectId, long pollId) {
        pollRepositoryJpa.deleteBySubject_IdAndId(subjectId, pollId);
    }

    private org.cooperative.poll.jpa.Poll mapToJpa(Poll poll) {
        Subject subject = subjectRepositoryJpa.findById(poll.getSubjectId())
                .orElseThrow(SubjectNotFoundException::new);
        return org.cooperative.poll.jpa.Poll.builder()
                .id(poll.getId() == null ? 0 : poll.getId())
                .name(poll.getName())
                .startDate(poll.getStartDate())
                .endDate(poll.getEndDate())
                .subject(subject)
                .build();
    }

    private Poll mapFromJpa(org.cooperative.poll.jpa.Poll poll) {
        return Poll.builder()
                .id(poll.getId())
                .name(poll.getName())
                .startDate(poll.getStartDate())
                .endDate(poll.getEndDate())
                .subjectId(poll.getSubject().getId())
                .build();
    }
}
