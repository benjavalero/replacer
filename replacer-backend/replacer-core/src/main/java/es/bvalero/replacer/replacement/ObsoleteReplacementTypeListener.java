package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.ObsoleteReplacementType;
import es.bvalero.replacer.finder.ObsoleteReplacementTypeObservable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
class ObsoleteReplacementTypeListener implements PropertyChangeListener {

    // Dependency injection
    private final ObsoleteReplacementTypeObservable obsoleteReplacementTypeObservable;
    private final ReplacementService replacementService;

    ObsoleteReplacementTypeListener(
        ObsoleteReplacementTypeObservable obsoleteReplacementTypeObservable,
        ReplacementService replacementService
    ) {
        this.obsoleteReplacementTypeObservable = obsoleteReplacementTypeObservable;
        this.replacementService = replacementService;
    }

    @PostConstruct
    public void init() {
        obsoleteReplacementTypeObservable.addPropertyChangeListener(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Collection<ObsoleteReplacementType> obsoleteList = (Collection<ObsoleteReplacementType>) evt.getNewValue();
        obsoleteList.forEach(obsolete ->
            replacementService.removeReplacementsByType(obsolete.getLang(), obsolete.getType())
        );
    }
}
