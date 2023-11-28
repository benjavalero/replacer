package es.bvalero.replacer.finder;

import java.beans.PropertyChangeListener;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface ObsoleteReplacementTypeObservable {
    void addPropertyChangeListener(PropertyChangeListener listener);
}
