package org.cooperative.poll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;

@AllArgsConstructor(staticName = "of")
@Value
@Builder
@With
public class Poll {
    Long id;
    String name;
    OffsetDateTime startDate;
    OffsetDateTime endDate;
    Long subjectId;
}
