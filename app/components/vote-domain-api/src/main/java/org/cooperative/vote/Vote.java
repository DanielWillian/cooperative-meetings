package org.cooperative.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor(staticName = "of")
@Value
@Builder
@With
public class Vote {
    UUID voter;
    boolean agree;
    OffsetDateTime voteDate;
    Long subjectId;
    Long pollId;
}
