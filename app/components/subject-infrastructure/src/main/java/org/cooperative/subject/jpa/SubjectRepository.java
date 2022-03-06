package org.cooperative.subject.jpa;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubjectRepository extends CrudRepository<Subject, Long> {
    List<Subject> findByName(String name);
}
