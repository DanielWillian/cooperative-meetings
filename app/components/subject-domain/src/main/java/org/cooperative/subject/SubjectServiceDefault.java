package org.cooperative.subject;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.subject.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class SubjectServiceDefault implements SubjectService {

    private SubjectRepository repository;
    private static final String WRONG_FORMAT_ID_MESSAGE = "Id has to be between 0 and " + Long.MAX_VALUE + "!";

    @Autowired
    public SubjectServiceDefault(SubjectRepository subjectRepository) {
        this.repository = subjectRepository;
    }

    @Override
    public void createSubject(Subject subject) {
        log.trace("ENTRY - subject: {}", subject);
        validateId(subject.getId());
        if (repository.existsById(subject.getId())) {
            log.error("Attempt to create subject that already exists: {}", subject);
            throw new SubjectAlreadyExistsException();
        }
        log.info("Subject create accepted: {}", subject);
        repository.save(subject);
        log.trace("EXIT");
    }

    @Override
    public Stream<Subject> getAllSubjects() {
        log.trace("ENTRY");
        Stream<Subject> subjectStream = repository.getAll();
        log.trace("EXIT");
        return subjectStream;
    }

    @Override
    public Optional<Subject> getSubjectById(long id) {
        log.trace("ENTRY - id: {}", id);
        validateId(id);
        Optional<Subject> optionalSubject = repository.getById(id);
        log.trace("EXIT");
        return optionalSubject;
    }

    @Override
    public Stream<Subject> getSubjectByName(String name) {
        log.trace("ENTRY - name: {}", name);
        Stream<Subject> subjectStream = repository.getByName(name);
        log.trace("EXIT");
        return subjectStream;
    }

    @Override
    public void updateSubject(Subject subject) {
        log.trace("ENTRY - subject: {}", subject);
        validateId(subject.getId());
        if (!repository.existsById(subject.getId())) {
            log.error("Attempt to update subject that does not exist: {}", subject);
            throw new SubjectNotFoundException();
        }
        log.info("Subject update accepted: {}", subject);
        repository.save(subject);
        log.trace("EXIT");
    }

    @Override
    public void deleteSubject(long id) {
        log.trace("ENTRY - id: {}", id);
        validateId(id);
        if (!repository.existsById(id)) {
            log.error("Attempt to delete subject that does not exist - id: {}", id);
            throw new SubjectNotFoundException();
        }
        log.info("Subject delete accepted - id: {}", id);
        repository.deleteById(id);
        log.trace("EXIT");
    }

    private void validateId(long id) {
        if (!isValidId(id)) {
            log.warn("Attempt to use invalid id: {}", id);
            throw new SubjectWrongFormatException(WRONG_FORMAT_ID_MESSAGE + " id: " + id);
        }
    }

    private boolean isValidId(long id) {
        return id >= 0;
    }
}
