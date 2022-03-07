package org.cooperative.vote.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.cooperative.vote.jpa.VoteCount;

@AllArgsConstructor(staticName = "of")
@Value
@Builder
@With
public class VoteCountImpl implements VoteCount {
    boolean agree;
    long count;
}
