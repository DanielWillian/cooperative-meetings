package org.cooperative.vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@AllArgsConstructor(staticName = "of")
@Value
@Builder
@With
public class VoteCount {
    long agree;
    long disagree;
}
