package org.cooperative.subject.api;

import org.cooperative.subject.SubjectService;
import org.cooperative.subject.api.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubjectsApiDefault implements SubjectsApiDelegate {

    @Autowired
    public SubjectsApiDefault(SubjectService subjectService) {
    }

    @Override
    public ResponseEntity<Subject> addSubject(Subject subject) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteSubjectById(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Subject> getSubjectById(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<List<Subject>> getSubjects() {
        return null;
    }

    @Override
    public ResponseEntity<Subject> updateSubject(Subject subject) {
        return null;
    }
}
