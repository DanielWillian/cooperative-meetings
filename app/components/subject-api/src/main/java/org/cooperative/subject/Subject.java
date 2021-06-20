package org.cooperative.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@AllArgsConstructor(staticName = "of")
@Value
@Builder
@ToString
public class Subject {
    long id;
    String name;
}
