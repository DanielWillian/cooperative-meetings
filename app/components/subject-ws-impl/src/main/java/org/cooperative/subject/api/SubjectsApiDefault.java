package org.cooperative.subject.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.subject.SubjectAlreadyExistsException;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.SubjectService;
import org.cooperative.subject.SubjectWrongFormatException;
import org.cooperative.subject.api.model.Error;
import org.cooperative.subject.api.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SubjectsApiDefault implements SubjectsApiDelegate {

    private static final String WRONG_FORMAT_ID_EXCEPTION =
            "Id must be between 0 and " + Long.MAX_VALUE + "!";
    private static final String SUBJECTS_BASE_URL = "/subjects";
    private static final String ENTRY = "ENTRY";
    private static final String EXIT = "EXIT";
    private SubjectService subjectService;

    @Autowired
    public SubjectsApiDefault(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Override
    public ResponseEntity<Subject> addSubject(Subject subject) {
        try {
            log.trace("{} - subject: {}", ENTRY, subject);
            subjectService.createSubject(mapToService(subject));
            log.trace(EXIT);
            return ResponseEntity.created(URI.create(SUBJECTS_BASE_URL + "/" + subject.getId()))
                    .body(subject);
        } catch (SubjectWrongFormatException e) {
            throw createExceptionForWrongFormat(subject.toString(), e);
        } catch (SubjectAlreadyExistsException e) {
            String message = "Attempt to add Subject that already exists: " + subject;
            log.info(message);
            throw SubjectsApiException.builder()
                    .throwable(e)
                    .message(message)
                    .status(HttpStatus.CONFLICT)
                    .build();
        } catch (Exception e) {
            log.error("Could not add subject - subject: " + subject, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Void> deleteSubjectById(Long id) {
        try {
            log.trace("{} - id: {}", ENTRY, id);
            subjectService.deleteSubject(id);
            log.trace(EXIT);
            return ResponseEntity
                    .noContent()
                    .build();
        } catch (SubjectWrongFormatException e) {
            throw createExceptionForWrongFormat(id.toString(), e);
        } catch (SubjectNotFoundException e) {
            throw createExceptionForNotFound(id.toString(), e);
        } catch (Exception e) {
            log.error("Could not delete subject - id: " + id, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Subject> getSubjectById(Long id) {
        try {
            log.trace("{} - id: {}", ENTRY, id);
            Optional<org.cooperative.subject.Subject> optionalSubject = subjectService.getSubjectById(id);
            Subject subject = optionalSubject
                    .map(this::mapFromService)
                    .orElseThrow(() -> createExceptionForNotFound(id.toString(), null));
            log.trace(EXIT);
            return ResponseEntity.ok(subject);
        } catch (SubjectWrongFormatException e) {
            throw createExceptionForWrongFormat(id.toString(), e);
        } catch (SubjectsApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Could not get subject - id: " + id, e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<List<Subject>> getSubjects() {
        try {
            log.trace(ENTRY);
            List<Subject> body = subjectService.getAllSubjects()
                    .map(this::mapFromService)
                    .collect(Collectors.toList());
            log.trace(EXIT);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("Could not get subjects!", e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<Subject> updateSubject(Subject subject) {
        try {
            log.trace("{} - subject: {}", ENTRY, subject);
            subjectService.updateSubject(mapToService(subject));
            log.trace(EXIT);
            return ResponseEntity.ok(subject);
        } catch (SubjectWrongFormatException e) {
            throw createExceptionForWrongFormat(subject.toString(), e);
        } catch (SubjectNotFoundException e) {
            throw createExceptionForNotFound(subject.toString(), e);
        } catch (Exception e) {
            log.error("Could not update subject - subject: " + subject, e);
            throw e;
        }
    }

    private SubjectsApiException createExceptionForWrongFormat(String subject, Exception e) {
        String message = "Subject provided is invalid: " + subject;
        log.info(message);
        return SubjectsApiException.builder()
                .throwable(e)
                .message(message)
                .status(HttpStatus.BAD_REQUEST)
                .error(createError(WRONG_FORMAT_ID_EXCEPTION))
                .build();
    }

    private SubjectsApiException createExceptionForNotFound(String subject, Exception e) {
        String message = "Subject not found: " + subject;
        log.info(message);
        return SubjectsApiException.builder()
                .throwable(e)
                .message(message)
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    private org.cooperative.subject.Subject mapToService(Subject subject) {
        return org.cooperative.subject.Subject.of(subject.getId(), subject.getName());
    }

    private Subject mapFromService(org.cooperative.subject.Subject subject) {
        Subject result = new Subject();
        result.setId(subject.getId());
        result.setName(subject.getName());
        return result;
    }

    private Error createError(String message) {
        Error error = new Error();
        error.setError(message);
        return error;
    }
}
