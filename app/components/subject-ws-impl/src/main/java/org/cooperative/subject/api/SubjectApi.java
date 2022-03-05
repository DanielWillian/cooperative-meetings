package org.cooperative.subject.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cooperative.subject.Subject;

@Slf4j
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class SubjectApi {
    private long id;
    private String name;

    public static SubjectApi fromDomain(Subject subject) {
        log.trace("fromDomain: {}", subject);
        return SubjectApi.of(subject.getId(), subject.getName());
    }

    public static Subject toDomain(SubjectApi subjectApi) {
        log.trace("toDomain: {}", subjectApi);
        return Subject.of(subjectApi.getId(), subjectApi.getName());
    }
}
