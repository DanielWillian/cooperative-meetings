package org.cooperative.vote;

import org.cooperative.vote.jpa.Vote;
import org.cooperative.vote.jpa.VoteRepositoryJpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class StubVoteRepositoryJpa implements VoteRepositoryJpa {
    private final List<Vote> votes = new ArrayList<>();
    private long nextId = 1;

    @Override
    public List<Vote> findByPoll_Subject_IdAndPoll_Id(long subjectId, long pollId) {
        return votes.stream()
                .filter(v -> v.getPoll().getSubject().getId() == subjectId && v.getPoll().getId() == pollId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Vote> findByPoll_Subject_IdAndPoll_IdAndVoter(long subjectId, long pollId, UUID voter) {
        return votes.stream()
                .filter(v -> v.getPoll().getSubject().getId() == subjectId && v.getPoll().getId() == pollId &&
                        v.getVoter().equals(voter))
                .findAny();
    }

    @Override
    public <S extends Vote> S save(S s) {
        if (s.getId() == null) s.setId(nextId++);
        else deleteById(s.getId());
        votes.add(s);
        return s;
    }

    @Override
    public <S extends Vote> Iterable<S> saveAll(Iterable<S> iterable) {
        for (S v : iterable) save(v);
        return iterable;
    }

    @Override
    public Optional<Vote> findById(Long aLong) {
        return votes.stream()
                .filter(v -> v.getId().equals(aLong))
                .findAny();
    }

    @Override
    public boolean existsById(Long aLong) {
        return votes.stream()
                .anyMatch(v -> v.getId().equals(aLong));
    }

    @Override
    public Iterable<Vote> findAll() {
        return votes;
    }

    @Override
    public Iterable<Vote> findAllById(Iterable<Long> iterable) {
        List<Vote> result = new ArrayList<>();
        for (Long l : iterable) findById(l).ifPresent(result::add);
        return result;
    }

    @Override
    public long count() {
        return votes.size();
    }

    @Override
    public void deleteById(Long aLong) {
        votes.removeIf(v -> v.getId().equals(aLong));
    }

    @Override
    public void delete(Vote vote) {
        votes.remove(vote);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> iterable) {
        for (Long l : iterable) deleteById(l);
    }

    @Override
    public void deleteAll(Iterable<? extends Vote> iterable) {
        for (Vote v : iterable) delete(v);
    }

    @Override
    public void deleteAll() {
        votes.clear();
    }
}
