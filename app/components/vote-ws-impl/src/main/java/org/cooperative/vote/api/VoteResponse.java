package org.cooperative.vote.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cooperative.vote.Vote;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class VoteResponse {
    private UUID voter;
    private boolean agree;
    private OffsetDateTime voteDate;

    public static VoteResponse fromDomain(Vote vote) {
        return VoteResponse.of(vote.getVoter(), vote.isAgree(), vote.getVoteDate());
    }
}
