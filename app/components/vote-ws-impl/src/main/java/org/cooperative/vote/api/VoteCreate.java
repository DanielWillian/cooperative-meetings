package org.cooperative.vote.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class VoteCreate {
    private UUID voter;
    private boolean agree;
}
