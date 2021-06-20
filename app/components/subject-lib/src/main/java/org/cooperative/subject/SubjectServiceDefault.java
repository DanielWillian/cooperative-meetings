package org.cooperative.subject;

import org.cooperative.subject.jpa.SubjectRepository;

import java.util.Optional;
import java.util.stream.Stream;

public class SubjectServiceDefault implements SubjectService {

    public SubjectServiceDefault(SubjectRepository subjectRepository) {
    }

    @Override
    public void createSubject(Subject subject) {
    }

    @Override
    public Stream<Subject> getAllSubjects() {
        return Stream.empty();
    }

    @Override
    public Optional<Subject> getSubjectById(long id) {
        return Optional.empty();
    }

    @Override
    public Stream<Subject> getSubjectByName(String name) {
        return Stream.empty();
    }

    @Override
    public void updateSubject(Subject subject) {
    }

    @Override
    public void deleteSubject(long id) {
    }
}
