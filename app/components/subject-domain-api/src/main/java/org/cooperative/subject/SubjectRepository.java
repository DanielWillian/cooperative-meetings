package org.cooperative.subject;

import java.util.Optional;
import java.util.stream.Stream;

public interface SubjectRepository {
    boolean existsById(long id);
    Subject save(Subject subject);
    Optional<Subject> getById(long id);
    Stream<Subject> getByName(String name);
    Stream<Subject> getAll();
    void deleteById(long id);
}
