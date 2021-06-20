package org.cooperative.subject;

import org.cooperative.subject.jpa.SubjectRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SubjectServiceTest {

    private SubjectRepository subjectRepository = new StubSubjectRepository();
    private SubjectService subjectService = new SubjectServiceDefault(subjectRepository);

    @Before
    public void beforeTest() {
        subjectRepository.deleteAll();
    }

    @Test
    public void testCreateSubjectSuccess() {
        subjectService.createSubject(Subject.of(0, "name"));
        Optional<org.cooperative.subject.jpa.Subject> optionalSubject = subjectRepository.findById(0L);
        assertTrue(optionalSubject.isPresent());
        assertEquals(org.cooperative.subject.jpa.Subject.of(0L, "name"), optionalSubject.get());
    }

    @Test
    public void testCreateSubjectAlreadyExists() {
        subjectService.createSubject(Subject.of(0, "name"));
        assertThrows(SubjectAlreadyExistsException.class,
                () -> subjectService.createSubject(Subject.of(0, "name")));
    }

    @Test
    public void testCreateSubjectWrongFormat() {
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.createSubject(Subject.of(-1, "name")));
    }

    @Test
    public void testUpdateSubjectSuccess() {
        subjectService.createSubject(Subject.of(0, "name"));
        subjectService.updateSubject(Subject.of(0, "updated"));
        Optional<org.cooperative.subject.jpa.Subject> optionalSubject = subjectRepository.findById(0L);
        assertTrue(optionalSubject.isPresent());
        assertEquals(org.cooperative.subject.jpa.Subject.of(0L, "updated"), optionalSubject.get());
    }

    @Test
    public void testUpdateSubjectNotFound() {
        assertThrows(SubjectNotFoundException.class,
                () -> subjectService.updateSubject(Subject.of(0, "name")));
    }

    @Test
    public void testUpdateSubjectWrongFormat() {
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.updateSubject(Subject.of(-1, "name")));
    }

    @Test
    public void testGetAllSubjectsNoSubjects() {
        List<Subject> allSubjects = subjectService.getAllSubjects()
                .collect(Collectors.toList());
        assertTrue(allSubjects.isEmpty());
    }

    @Test
    public void testGetAllSubjectsSomeSubjects() {
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(0L, "name0"));
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(1L, "name1"));
        List<Subject> allSubjects = subjectService.getAllSubjects()
                .collect(Collectors.toList());
        Subject[] expectedSubjects = new Subject[] { Subject.of(0, "name0"), Subject.of(1, "name1") };
        assertThat(allSubjects, containsInAnyOrder(expectedSubjects));
    }

    @Test
    public void testGetSubjectByIdNotFound() {
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(0L, "name"));
        List<Subject> allSubjects = subjectService.getAllSubjects()
                .collect(Collectors.toList());
        assertFalse(allSubjects.isEmpty());
        Optional<Subject> optionalSubject = subjectService.getSubjectById(1);
        assertFalse(optionalSubject.isPresent());
    }

    @Test
    public void testGetSubjectByIdWrongFormat() {
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.getSubjectById(-1));
    }

    @Test
    public void testGetSubjectByNameNoSubjects() {
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(0L, "nameA"));
        List<Subject> allSubjects = subjectService.getAllSubjects()
                .collect(Collectors.toList());
        assertFalse(allSubjects.isEmpty());
        List<Subject> nameBSubjects = subjectService.getSubjectByName("nameB")
                .collect(Collectors.toList());
        assertTrue(nameBSubjects.isEmpty());
    }

    @Test
    public void testGetSubjectByNameSomeSubjects() {
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(0L, "nameA"));
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(1L, "nameB"));
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(2L, "nameA"));
        List<Subject> subjects = subjectService.getSubjectByName("nameA")
                .collect(Collectors.toList());
        Subject[] expectedSubjects = new Subject[] { Subject.of(0, "nameA"), Subject.of(2, "nameA") };
        assertThat(subjects, containsInAnyOrder(expectedSubjects));
    }

    @Test
    public void testDeleteSubjectSuccess() {
        subjectRepository.save(org.cooperative.subject.jpa.Subject.of(0L, "name"));
        subjectService.deleteSubject(0);
        Optional<Subject> deletedSubject = subjectService.getSubjectById(0);
        assertFalse(deletedSubject.isPresent());
    }

    @Test
    public void testDeleteSubjectNotFound() {
        assertThrows(SubjectNotFoundException.class,
                () -> subjectService.deleteSubject(0));
    }

    @Test
    public void testDeleteSubjectWrongFormat() {
        assertThrows(SubjectWrongFormatException.class,
                () -> subjectService.deleteSubject(-1));
    }

}
