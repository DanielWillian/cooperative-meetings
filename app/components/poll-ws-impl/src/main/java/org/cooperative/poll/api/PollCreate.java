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
public class PollCreate {
    private String name;
    private OffsetDateTime endDate;

    public static Poll toDomain(PollCreate pollCreate, long subjectId) {
        return Poll.builder()
                .name(pollCreate.getName())
                .startDate(OffsetDateTime.now())
                .endDate(pollCreate.getEndDate())
                .subjectId(subjectId)
                .build();
    }
}
