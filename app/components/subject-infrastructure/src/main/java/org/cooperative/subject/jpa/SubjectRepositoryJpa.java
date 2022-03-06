package org.cooperative.subject.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubjectRepositoryJpa extends CrudRepository<Subject, Long> {
    List<Subject> findByName(String name);
}
