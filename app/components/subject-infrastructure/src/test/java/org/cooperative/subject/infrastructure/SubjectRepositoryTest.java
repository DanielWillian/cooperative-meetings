package org.cooperative.subject.infrastructure;

import org.cooperative.subject.Subject;
import org.cooperative.subject.jpa.SubjectRepositoryJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectRepositoryTest {
    @InjectMocks
    SubjectRepositoryImpl subjectRepository;

    @Mock
    SubjectRepositoryJpa subjectRepositoryJpa;

    @Test
    void testExistsById() {
        subjectRepository.existsById(0);
        verify(subjectRepositoryJpa, times(1))
                .existsById(0L);
    }

    @Test
    void testSave() {
        when(subjectRepositoryJpa.save(org.cooperative.subject.jpa.Subject.of(0L, "name")))
                .thenReturn(org.cooperative.subject.jpa.Subject.of(0L, "name"));
        Subject subject = subjectRepository.save(Subject.of(0, "name"));
        verify(subjectRepositoryJpa, times(1))
                .save(org.cooperative.subject.jpa.Subject.of(0L, "name"));
        assertEquals(Subject.of(0, "name"), subject);
    }

    @Test
    void testGetById() {
        when(subjectRepositoryJpa.findById(0L))
                .thenReturn(Optional.of(org.cooperative.subject.jpa.Subject.of(0L, "name")));
        Optional<Subject> subject = subjectRepository.getById(0);
        assertEquals(Subject.of(0, "name"), subject.get());
    }

    @Test
    void testGetByName() {
        when(subjectRepositoryJpa.findByName("name"))
                .thenReturn(Arrays.asList(org.cooperative.subject.jpa.Subject.of(0L, "name")));
        List<Subject> subject = subjectRepository.getByName("name")
                .collect(Collectors.toList());
        assertEquals(Subject.of(0, "name"), subject.get(0));
    }

    @Test
    void testGetAll() {
        when(subjectRepositoryJpa.findAll())
                .thenReturn(Arrays.asList(org.cooperative.subject.jpa.Subject.of(0L, "name")));
        List<Subject> subject = subjectRepository.getAll()
                .collect(Collectors.toList());
        assertEquals(Subject.of(0, "name"), subject.get(0));
    }

    @Test
    void testDeleteById() {
        subjectRepository.deleteById(0);
        verify(subjectRepositoryJpa, times(1))
                .deleteById(0L);
    }
}
