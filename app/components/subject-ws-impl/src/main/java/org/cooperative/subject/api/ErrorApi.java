package org.cooperative.subject.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
@Builder
public class ErrorApi {
    private String error;
}
