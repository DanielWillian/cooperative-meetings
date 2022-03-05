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
public class PollUpdate {
    private long id;
    private String name;
    private OffsetDateTime endDate;

    public static Poll toDomain(PollUpdate pollUpdate, long subjectId) {
        return Poll.builder()
                .id(pollUpdate.getId())
                .name(pollUpdate.getName())
                .startDate(OffsetDateTime.now())
                .endDate(pollUpdate.getEndDate())
                .subjectId(subjectId)
                .build();
    }
}
