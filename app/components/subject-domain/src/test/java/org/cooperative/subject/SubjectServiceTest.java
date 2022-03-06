package org.cooperative.subject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @InjectMocks
    SubjectServiceDefault subjectService;

    @Mock
    SubjectRepository subjectRepository;

    @Test
    void testCreateSubjectSuccess() {
        when(subjectRepository.existsById(0)).thenReturn(false);
        subjectService.createSubject(Subject.of(0, "name"));
        verify(subjectRepository, times(1))
                .save(Subject.of(0, "name"));
    }

    @Test
    void testCreateSubjectAlreadyExists() {
        when(subjectRepository.existsById(0)).thenReturn(true);
        Subject subject = Subject.of(0, "name");
        assertThrows(SubjectAlreadyExistsException.class,
                () -> subjectService.createSubject(subject));
    }

    @Test
    void testCreateSubjectWrongFormat() {
        Subject subject = Subject.of(-1, "name");
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.createSubject(subject));
    }

    @Test
    void testUpdateSubjectSuccess() {
        when(subjectRepository.existsById(0)).thenReturn(true);
        subjectService.updateSubject(Subject.of(0, "updated"));
        verify(subjectRepository, times(1))
                .save(Subject.of(0, "updated"));
    }

    @Test
    void testUpdateSubjectNotFound() {
        when(subjectRepository.existsById(0)).thenReturn(false);
        Subject subject = Subject.of(0, "name");
        assertThrows(SubjectNotFoundException.class,
                () -> subjectService.updateSubject(subject));
    }

    @Test
    void testUpdateSubjectWrongFormat() {
        Subject subject = Subject.of(-1, "name");
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.updateSubject(subject));
    }

    @Test
    void testGetAllSubjectsNoSubjects() {
        when(subjectRepository.getAll()).thenReturn(Stream.empty());
        List<Subject> allSubjects = subjectService.getAllSubjects()
                .collect(Collectors.toList());
        assertTrue(allSubjects.isEmpty());
    }

    @Test
    void testGetAllSubjectsSomeSubjects() {
        when(subjectRepository.getAll()).thenReturn(
                Stream.of(Subject.of(0L, "name0"), Subject.of(1L, "name1")));
        List<Subject> allSubjects = subjectService.getAllSubjects()
                .collect(Collectors.toList());
        Subject[] expectedSubjects = new Subject[] { Subject.of(0, "name0"), Subject.of(1, "name1") };
        assertThat(allSubjects, containsInAnyOrder(expectedSubjects));
    }

    @Test
    void testGetSubjectByIdNotFound() {
        when(subjectRepository.getById(1)).thenReturn(Optional.empty());
        Optional<Subject> optionalSubject = subjectService.getSubjectById(1);
        assertFalse(optionalSubject.isPresent());
    }

    @Test
    void testGetSubjectByIdWrongFormat() {
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.getSubjectById(-1));
    }

    @Test
    public void testGetSubjectByNameNoSubjects() {
        when(subjectRepository.getByName("name")).thenReturn(Stream.empty());
        List<Subject> subjects = subjectService.getSubjectByName("name")
                .collect(Collectors.toList());
        assertTrue(subjects.isEmpty());
    }

    @Test
    public void testGetSubjectByNameSomeSubjects() {
        when(subjectRepository.getByName("nameA")).thenReturn(Stream.of(
                Subject.of(0, "nameA"),
                Subject.of(2, "nameA")
        ));
        List<Subject> subjects = subjectService.getSubjectByName("nameA")
                .collect(Collectors.toList());
        Subject[] expectedSubjects = new Subject[] { Subject.of(0, "nameA"), Subject.of(2, "nameA") };
        assertThat(subjects, containsInAnyOrder(expectedSubjects));
    }

    @Test
    public void testDeleteSubjectSuccess() {
        when(subjectRepository.existsById(0)).thenReturn(true);
        subjectService.deleteSubject(0);
        verify(subjectRepository, times(1))
                .deleteById(0);
    }

    @Test
    public void testDeleteSubjectNotFound() {
        when(subjectRepository.existsById(0)).thenReturn(false);
        assertThrows(SubjectNotFoundException.class,
                () -> subjectService.deleteSubject(0));
    }

    @Test
    public void testDeleteSubjectWrongFormat() {
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.deleteSubject(-1));
    }

}
