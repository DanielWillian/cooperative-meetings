package org.cooperative.poll.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cooperative.poll.Poll;
import org.cooperative.vote.VoteCount;

import java.time.OffsetDateTime;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class PollVotesResponse {
    private long id;
    private String name;
    private OffsetDateTime endDate;
    private VotesResponse votes;

    public static PollVotesResponse fromDomain(Poll poll, VoteCount voteCount) {
        return PollVotesResponse.builder()
                .id(poll.getId())
                .name(poll.getName())
                .endDate(poll.getEndDate())
                .votes(VotesResponse.of(voteCount.getAgree(), voteCount.getDisagree()))
                .build();
    }
}
