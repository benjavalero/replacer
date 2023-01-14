package es.bvalero.replacer.replacement;

import es.bvalero.replacer.finder.ObsoleteReplacementType;
import es.bvalero.replacer.finder.ObsoleteReplacementTypeObservable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ObsoleteReplacementTypeListener implements PropertyChangeListener {

    @Autowired
    private ObsoleteReplacementTypeObservable obsoleteReplacementTypeObservable;

    @PostConstruct
    public void init() {
        obsoleteReplacementTypeObservable.addPropertyChangeListener(this);
    }

    @Autowired
    private ReplacementService replacementService;

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Collection<ObsoleteReplacementType> obsoleteList = (Collection<ObsoleteReplacementType>) evt.getNewValue();
        obsoleteList.forEach(obsolete ->
            replacementService.removeReplacementsByType(obsolete.getLang(), obsolete.getType())
        );
    }
}
