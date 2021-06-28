package org.cooperative.poll;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.poll.exception.PollNotFoundException;
import org.cooperative.poll.exception.PollValidationException;
import org.cooperative.poll.exception.Validation;
import org.cooperative.poll.jpa.PollRepository;
import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
public class PollServiceDefault implements PollService {

    private static final int NAME_MAX_LENGTH = 200;
    private static final Duration DEFAULT_END_DURATION = Duration.ofMinutes(1);
    private final PollRepository pollRepository;
    private final SubjectService subjectService;

    @Autowired
    public PollServiceDefault(PollRepository pollRepository, SubjectService service) {
        this.pollRepository = pollRepository;
        this.subjectService = service;
    }

    @Override
    public Poll createPoll(Poll poll) {
        log.trace("ENTRY - poll: {}", poll);
        validatePollForCreate(poll);
        if (poll.getEndDate() == null) {
            poll = poll.withEndDate(poll.getStartDate().plus(DEFAULT_END_DURATION));
        }
        Subject subject = subjectService.getSubjectById(poll.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException());

        org.cooperative.poll.jpa.Poll toBeSaved = org.cooperative.poll.jpa.Poll.builder()
                .name(poll.getName())
                .startDate(poll.getStartDate())
                .endDate(poll.getEndDate())
                .subject(org.cooperative.subject.jpa.Subject.of(subject.getId(), null))
                .build();

        pollRepository.save(toBeSaved);

        log.info("Created poll: {}", toBeSaved);

        Poll returnPoll = mapFromJpa(toBeSaved);

        log.trace("EXIT");
        return returnPoll;
    }

    private void validatePollForCreate(Poll poll) {
        List<Validation> validations = new ArrayList<>();
        if (poll.getStartDate() == null) validations.add(Validation.MISSING_START_DATE);
        if (poll.getSubjectId() == null) validations.add(Validation.MISSING_SUBJECT_ID);
        if (poll.getName() != null && poll.getName().length() > NAME_MAX_LENGTH) {
            validations.add(Validation.NAME_TOO_LONG);
        }
        if (poll.getStartDate() != null && poll.getEndDate() != null &&
                !poll.getStartDate().isBefore(poll.getEndDate())) {
            validations.add(Validation.END_DATE_EARLIER_THAN_START_DATE);
        }
        if (!validations.isEmpty()) {
            throw PollValidationException.builder()
                    .validations(validations)
                    .build();
        }
    }

    @Override
    public Stream<Poll> getPollBySubjectId(long subjectId) {
        log.trace("ENTRY - subjectId: {}", subjectId);

        Stream<Poll> polls = pollRepository.findBySubject_Id(subjectId).stream()
                .map(this::mapFromJpa);

        log.trace("EXIT");
        return polls;
    }

    @Override
    public Optional<Poll> getPollByIdAndSubjectId(long id, long subjectId) {
        log.trace("ENTRY - id: {}, subjectId: {}", id, subjectId);

        Optional<Poll> polls = pollRepository.findBySubject_IdAndId(subjectId, id)
                .map(this::mapFromJpa);

        log.trace("EXIT");
        return polls;
    }

    @Override
    public Stream<Poll> getPollByNameAndSubjectId(String name, long subjectId) {
        log.trace("ENTRY - subjectId: {}", subjectId);

        Stream<Poll> polls = pollRepository.findBySubject_IdAndName(subjectId, name).stream()
                .map(this::mapFromJpa);

        log.trace("EXIT");
        return polls;
    }

    @Override
    public Poll updatePoll(Poll poll) {
        log.trace("ENTRY - poll: {}", poll);
        validatePollForUpdate(poll);
        if (subjectService.getSubjectById(poll.getSubjectId()).isEmpty()) {
            throw new SubjectNotFoundException();
        }
        org.cooperative.poll.jpa.Poll currentPoll = pollRepository.findById(poll.getId())
                .orElseThrow(PollNotFoundException::new);

        currentPoll.setName(poll.getName());
        if (poll.getStartDate() != null) currentPoll.setStartDate(poll.getStartDate());
        if (poll.getEndDate() != null) currentPoll.setEndDate(poll.getEndDate());

        if (!currentPoll.getStartDate().isBefore(currentPoll.getEndDate())) {
            throw PollValidationException.builder()
                    .validation(Validation.END_DATE_EARLIER_THAN_START_DATE)
                    .build();
        }

        pollRepository.save(currentPoll);

        log.info("Updated poll: {}", currentPoll);

        Poll returnPoll = mapFromJpa(currentPoll);

        log.trace("EXIT");
        return returnPoll;
    }

    private void validatePollForUpdate(Poll poll) {
        List<Validation> validations = new ArrayList<>();
        if (poll.getId() == null) validations.add(Validation.MISSING_ID);
        if (poll.getSubjectId() == null) validations.add(Validation.MISSING_SUBJECT_ID);
        if (poll.getName() != null && poll.getName().length() > NAME_MAX_LENGTH) {
            validations.add(Validation.NAME_TOO_LONG);
        }
        if (!validations.isEmpty()) {
            throw PollValidationException.builder()
                    .validations(validations)
                    .build();
        }
    }

    @Override
    public void deletePollByIdAndSubjectId(long id, long subjectId) {
        log.trace("ENTRY - id: {}, subjectId: {}", id, subjectId);

        Optional<Poll> polls = pollRepository.findBySubject_IdAndId(subjectId, id)
                .map(this::mapFromJpa);

        if (subjectService.getSubjectById(subjectId).isEmpty()) {
            throw new SubjectNotFoundException();
        }
        if (pollRepository.findBySubject_IdAndId(subjectId, id).isEmpty()) {
            throw new PollNotFoundException();
        }

        pollRepository.deleteById(id);

        log.info("Deleted poll - id: {}, subjectId: {}", id, subjectId);

        log.trace("EXIT");
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
