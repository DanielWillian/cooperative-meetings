package org.cooperative.subject.api;

import lombok.extern.slf4j.Slf4j;
import org.cooperative.subject.Subject;
import org.cooperative.subject.SubjectNotFoundException;
import org.cooperative.subject.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/subjects")
public class SubjectsApiController {

    private static final String EXIT = "EXIT";
    private final SubjectService subjectService;

    @Autowired
    public SubjectsApiController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public Flux<SubjectApi> getSubjects() {
        log.info("Received get all subjects");
        return Flux.defer(this::getSubjectsFromService)
                .map(SubjectApi::fromDomain);
    }

    private Flux<Subject> getSubjectsFromService() {
        log.trace("ENTRY - get subjects from service");
        Flux<Subject> subjects = Flux.fromStream(subjectService::getAllSubjects)
                .subscribeOn(Schedulers.boundedElastic());
        log.trace("EXIT - get subjects from service");
        return subjects;
    }

    @GetMapping("/{id}")
    public Mono<SubjectApi> getSubjectById(@PathVariable("id") long id) {
        log.info("Received get subject by id {}", id);
        return Mono.defer(() -> getSubjectByIdFromService(id))
                .map(SubjectApi::fromDomain);
    }

    private Mono<Subject> getSubjectByIdFromService(long id) {
        log.trace("ENTRY - get subject by id from service: {}", id);
        Mono<Subject> subject = Mono.fromCallable(() -> subjectService.getSubjectById(id)
                        .orElseThrow(() -> new SubjectNotFoundException(String.valueOf(id))))
                .subscribeOn(Schedulers.boundedElastic());
        log.trace("EXIT - get subject by id from service: {}", id);
        return subject;
    }

    @PostMapping
    public Mono<ResponseEntity<SubjectApi>> addSubject(@RequestBody SubjectApi subjectApi) {
        log.info("Received add subject: {}", subjectApi);
        return Mono.defer(() -> addSubjectFromService(subjectApi))
                .map(SubjectApi::fromDomain)
                .map(s -> ResponseEntity.created(URI.create("/subjects/" + s.getId())).body(s));
    }

    private Mono<Subject> addSubjectFromService(SubjectApi subjectApi) {
        log.trace("ENTRY - add subject from service: {}", subjectApi);
        Mono<Subject> subject = Mono.just(SubjectApi.toDomain(subjectApi))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(subjectService::createSubject);
        log.trace("EXIT - add subject from service: {}", subjectApi);
        return subject;
    }

    @PutMapping
    public Mono<SubjectApi> updateSubject(@RequestBody SubjectApi subjectApi) {
        log.info("Received update subject: {}", subjectApi);
        return Mono.defer(() -> updateSubjectFromService(subjectApi))
                .map(SubjectApi::fromDomain);
    }

    private Mono<Subject> updateSubjectFromService(SubjectApi subjectApi) {
        log.trace("ENTRY - update subject from service: {}", subjectApi);
        Mono<Subject> subject = Mono.just(SubjectApi.toDomain(subjectApi))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(subjectService::updateSubject);
        log.trace("EXIT - update subject from service: {}", subjectApi);
        return subject;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteSubjectById(@PathVariable("id") long id) {
        log.info("Received delete subject by id {}", id);
        return Mono.defer(() -> deleteSubjectByIdFromService(id));
    }

    private Mono<Void> deleteSubjectByIdFromService(long id) {
        log.trace("ENTRY - delete subject by id from service: {}", id);
        Mono<Void> mono = Mono.just(id)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(subjectService::deleteSubject)
                .then();
        log.trace("EXIT - delete subject by id from service: {}", id);
        return mono;
    }
}
