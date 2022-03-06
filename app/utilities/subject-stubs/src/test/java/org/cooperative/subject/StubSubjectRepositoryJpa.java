package org.cooperative.subject;

import org.cooperative.subject.jpa.Subject;
import org.cooperative.subject.jpa.SubjectRepositoryJpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StubSubjectRepositoryJpa implements SubjectRepositoryJpa {
    private final List<Subject> subjects = new ArrayList<>();

    @Override
    public List<Subject> findByName(String name) {
        return subjects.stream()
                .filter(s -> s.getName().equals(name))
                .collect(Collectors.toList());
    }

    @Override
    public <S extends Subject> S save(S s) {
        if (existsById(s.getId())) delete(s);
        subjects.add(s);
        return s;
    }

    @Override
    public <S extends Subject> Iterable<S> saveAll(Iterable<S> iterable) {
        for (S s : iterable) save(s);
        return iterable;
    }

    @Override
    public Optional<Subject> findById(Long aLong) {
        return subjects.stream()
                .filter(s -> s.getId().equals(aLong))
                .findAny();
    }

    @Override
    public boolean existsById(Long aLong) {
        return subjects.stream()
                .anyMatch(s -> s.getId().equals(aLong));
    }

    @Override
    public Iterable<Subject> findAll() {
        return subjects;
    }

    @Override
    public Iterable<Subject> findAllById(Iterable<Long> iterable) {
        List<Subject> result = new ArrayList<>();
        for (Long l : iterable) findById(l).ifPresent(result::add);
        return result;
    }

    @Override
    public long count() {
        return subjects.size();
    }

    @Override
    public void deleteById(Long aLong) {
        subjects.removeIf(s -> s.getId().equals(aLong));
    }

    @Override
    public void delete(Subject subject) {
        subjects.remove(subject);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> iterable) {
        for (Long l : iterable) deleteById(l);
    }

    @Override
    public void deleteAll(Iterable<? extends Subject> iterable) {
        for (Subject s : iterable) delete(s);
    }

    @Override
    public void deleteAll() {
        subjects.clear();
    }
}
