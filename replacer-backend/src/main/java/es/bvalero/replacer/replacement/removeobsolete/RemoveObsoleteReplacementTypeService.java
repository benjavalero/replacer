package es.bvalero.replacer.replacement.removeobsolete;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.RemoveObsoleteReplacementType;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class RemoveObsoleteReplacementTypeService implements RemoveObsoleteReplacementType {

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    @Override
    public void removeObsoleteReplacementTypes(WikipediaLanguage lang, Collection<ReplacementType> types) {
        replacementTypeRepository.removeReplacementsByType(lang, types);
    }
}
