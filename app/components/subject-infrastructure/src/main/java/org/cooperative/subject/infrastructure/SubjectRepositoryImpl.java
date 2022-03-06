package org.cooperative.subject.infrastructure;

import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectRepository;
import org.cooperative.subject.jpa.SubjectRepositoryJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class SubjectRepositoryImpl implements SubjectRepository {
    private final SubjectRepositoryJpa repository;

    @Autowired
    public SubjectRepositoryImpl(SubjectRepositoryJpa repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(long id) {
        return repository.existsById(id);
    }

    @Override
    public Subject save(Subject subject) {
        return mapFromJpa(repository.save(mapToJpa(subject)));
    }

    @Override
    public Optional<Subject> getById(long id) {
        return repository.findById(id)
                .map(this::mapFromJpa);
    }

    @Override
    public Stream<Subject> getByName(String name) {
        return repository.findByName(name).stream()
                .map(this::mapFromJpa);
    }

    @Override
    public Stream<Subject> getAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(this::mapFromJpa);
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }

    private org.cooperative.subject.jpa.Subject mapToJpa(Subject subject) {
        return org.cooperative.subject.jpa.Subject.of(subject.getId(), subject.getName());
    }

    private Subject mapFromJpa(org.cooperative.subject.jpa.Subject subject) {
        return Subject.of(subject.getId(), subject.getName());
    }
}
