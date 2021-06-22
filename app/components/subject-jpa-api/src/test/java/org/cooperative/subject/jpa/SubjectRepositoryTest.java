package org.cooperative.subject.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;

    @Test
    public void findById() {
        Subject subject = Subject.of(1L, "name");
        subjectRepository.save(subject);
        Optional<Subject> optionalSubject = subjectRepository.findById(1L);
        assertTrue(optionalSubject.isPresent());
        assertEquals(subject, optionalSubject.get());
    }

    @Test
    public void findByName() {
        Subject subject = Subject.of(1L, "name");
        subjectRepository.save(subject);
        List<Subject> optionalSubject = subjectRepository.findByName("name");
        assertFalse(optionalSubject.isEmpty());
        assertEquals(subject, optionalSubject.get(0));
    }
}
