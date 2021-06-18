package org.cooperative.subject.jpa;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
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
        Assert.assertTrue(optionalSubject.isPresent());
        Assert.assertEquals(subject, optionalSubject.get());
    }

    @Test
    public void findByName() {
        Subject subject = Subject.of(1L, "name");
        subjectRepository.save(subject);
        List<Subject> optionalSubject = subjectRepository.findByName("name");
        Assert.assertFalse(optionalSubject.isEmpty());
        Assert.assertEquals(subject, optionalSubject.get(0));
    }
}
