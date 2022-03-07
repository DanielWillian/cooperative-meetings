package org.cooperative.vote.jpa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cooperative.poll.jpa.Poll;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class Vote {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private UUID voter;

    @Column(nullable = false)
    private boolean agree;

    @Column(name = "vote_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime voteDate;

    @ManyToOne(optional = false)
    private Poll poll;
}
