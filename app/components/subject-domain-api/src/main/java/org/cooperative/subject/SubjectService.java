package org.cooperative.subject;

import java.util.Optional;
import java.util.stream.Stream;

public interface SubjectService {
    void createSubject(Subject subject);
    Stream<Subject> getAllSubjects();
    Optional<Subject> getSubjectById(long id);
    Stream<Subject> getSubjectByName(String name);
    void updateSubject(Subject subject);
    void deleteSubject(long id);
}
