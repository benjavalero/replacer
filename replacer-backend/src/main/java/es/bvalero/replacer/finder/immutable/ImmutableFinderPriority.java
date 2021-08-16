package es.bvalero.replacer.finder.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImmutableFinderPriority {
    NONE(0),
    LOW(2),
    MEDIUM(5),
    HIGH(12),
    VERY_HIGH(25),
    MAX(100);

    private final int value;
}
