package org.cooperative.poll.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cooperative.poll.Poll;

import java.time.OffsetDateTime;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Data
@Builder
public class PollResponse {
    private long id;
    private String name;
    private OffsetDateTime endDate;

    public static PollResponse fromDomain(Poll poll) {
        return PollResponse.builder()
                .id(poll.getId())
                .name(poll.getName())
                .endDate(poll.getEndDate())
                .build();
    }
}
