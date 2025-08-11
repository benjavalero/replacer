package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class RemovedTypeEvent {

    ChangedReplacementType replacementType;
}
