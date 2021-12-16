package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.load.ObsoleteMisspellingListener;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ObsoleteReplacementListener extends ObsoleteMisspellingListener {

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    @Override
    protected void processObsoleteReplacementTypes(WikipediaLanguage lang, Collection<ReplacementType> types) {
        replacementTypeRepository.removeReplacementsByType(lang, types);
    }
}
