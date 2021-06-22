package org.cooperative.subject.api;

import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectAlreadyExistsException;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.SubjectService;
import org.cooperative.subject.SubjectWrongFormatException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class StubSubjectService implements SubjectService {
    private List<Subject> subjects = new ArrayList<>();

    @Override
    public void createSubject(Subject subject) {
        if (getSubjectById(subject.getId()).isPresent()) throw new SubjectAlreadyExistsException();
        subjects.add(subject);
    }

    @Override
    public Stream<Subject> getAllSubjects() {
        return subjects.stream();
    }

    @Override
    public Optional<Subject> getSubjectById(long id) {
        if (!isValid(id)) throw new SubjectWrongFormatException();
        return subjects.stream()
                .filter(s -> s.getId() == id)
                .findAny();
    }

    @Override
    public Stream<Subject> getSubjectByName(String name) {
        return subjects.stream()
                .filter(s -> s.getName().equals(name));
    }

    @Override
    public void updateSubject(Subject subject) {
        if (getSubjectById(subject.getId()).isEmpty()) throw new SubjectNotFoundException();
        deleteSubject(subject.getId());
        subjects.add(subject);
    }

    @Override
    public void deleteSubject(long id) {
        if (getSubjectById(id).isEmpty()) throw new SubjectNotFoundException();
        subjects.removeIf(s -> s.getId() == id);
    }

    public void deleteAllSubjects() {
        subjects.clear();
    }

    private boolean isValid(long id) {
        return id >= 0;
    }
}
