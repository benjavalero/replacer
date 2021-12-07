package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.load.ObsoleteMisspellingListener;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ObsoleteReplacementListener extends ObsoleteMisspellingListener {

    @Autowired
    private ReplacementCountService replacementCountService;

    @Override
    protected void processObsoleteReplacementTypes(
        WikipediaLanguage lang,
        ReplacementType type,
        Collection<String> subtypes
    ) {
        replacementCountService.removeReplacementsByType(lang, type.getLabel(), subtypes);
    }
}
