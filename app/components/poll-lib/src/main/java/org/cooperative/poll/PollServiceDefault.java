package org.cooperative.poll;

import org.cooperative.poll.jpa.PollRepository;
import org.cooperative.subject.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class PollServiceDefault implements PollService {

    private PollRepository pollRepository;
    private SubjectService subjectService;

    @Autowired
    public PollServiceDefault(PollRepository pollRepository, SubjectService service) {
        this.pollRepository = pollRepository;
        this.subjectService = service;
    }

    @Override
    public Poll createPoll(Poll poll) {
        return null;
    }

    @Override
    public Stream<Poll> getPollBySubjectId(long subjectId) {
        return null;
    }

    @Override
    public Optional<Poll> getPollByIdAndSubjectId(long id, long subjectId) {
        return null;
    }

    @Override
    public Stream<Poll> getPollByNameAndSubjectId(String name, long subjectId) {
        return null;
    }

    @Override
    public Poll updatePoll(Poll poll) {
        return null;
    }

    @Override
    public void deletePollByIdAndSubjectId(long id, long subjectId) {
    }
}
