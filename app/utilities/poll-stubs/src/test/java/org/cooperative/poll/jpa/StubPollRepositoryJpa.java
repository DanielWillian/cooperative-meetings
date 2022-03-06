package org.cooperative.poll.jpa;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StubPollRepositoryJpa implements PollRepositoryJpa {

    List<Poll> polls = new ArrayList<>();
    long nextId = 1;

    @Override
    public List<Poll> findBySubject_Id(long subjectId) {
        return polls.stream()
                .filter(p -> p.getSubject().getId() == subjectId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Poll> findBySubject_IdAndId(long subjectId, long pollId) {
        return polls.stream()
                .filter(p -> p.getId() == pollId && p.getSubject().getId() == subjectId)
                .findAny();
    }

    @Override
    public List<Poll> findBySubject_IdAndName(long subjectId, String name) {
        return polls.stream()
                .filter(p -> p.getName().equals(name) && p.getSubject().getId() == subjectId)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBySubject_IdAndId(long subjectId, long pollId) {
        polls.removeIf(p -> p.getId() == pollId && p.getSubject().getId() == subjectId);
    }

    @Override
    public <S extends Poll> S save(S s) {
        if (s.getId() == 0) s.setId(nextId++);
        else deleteById(s.getId());
        polls.add(s);
        return s;
    }

    @Override
    public <S extends Poll> Iterable<S> saveAll(Iterable<S> iterable) {
        for (S p : iterable) save(p);
        return iterable;
    }

    @Override
    public Optional<Poll> findById(Long aLong) {
        return polls.stream()
                .filter(p -> p.getId() == aLong)
                .findAny();
    }

    @Override
    public boolean existsById(Long aLong) {
        return polls.stream()
                .anyMatch(p -> p.getId() == aLong);
    }

    @Override
    public Iterable<Poll> findAll() {
        return polls;
    }

    @Override
    public Iterable<Poll> findAllById(Iterable<Long> iterable) {
        List<Poll> result = new ArrayList<>();
        for (Long l : iterable) findById(l).ifPresent(result::add);
        return result;
    }

    @Override
    public long count() {
        return polls.size();
    }

    @Override
    public void deleteById(Long aLong) {
        polls.removeIf(p -> p.getId() == aLong);
    }

    @Override
    public void delete(Poll poll) {
        polls.remove(poll);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> iterable) {
        for (Long l : iterable) deleteById(l);
    }

    @Override
    public void deleteAll(Iterable<? extends Poll> iterable) {
        for (Poll p : iterable) delete(p);
    }

    @Override
    public void deleteAll() {
        polls.clear();
        nextId = 1;
    }
}
