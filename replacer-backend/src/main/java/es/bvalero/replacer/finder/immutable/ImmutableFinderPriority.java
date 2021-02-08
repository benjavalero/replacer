package es.bvalero.replacer.finder.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
enum ImmutableFinderPriority {
    NONE(0),
    LOW(2),
    MEDIUM(5),
    HIGH(12),
    VERY_HIGH(25);

    private final int value;
}
