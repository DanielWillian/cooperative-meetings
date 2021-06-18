package org.cooperative.subject.jpa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@ToString
public class Subject {
    @Id
    Long id;
    String name;
}
