package org.cooperative.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@AllArgsConstructor(staticName = "of")
@Value
@Builder
@With
public class Subject {
    long id;
    String name;
}
