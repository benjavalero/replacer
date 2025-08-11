package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class AddedTypeEvent {

    ChangedReplacementType replacementType;
}
