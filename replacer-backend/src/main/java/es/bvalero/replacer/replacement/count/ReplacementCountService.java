package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ReplacementCountService {

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    Collection<ResultCount<ReplacementType>> countReplacementsGroupedByType(WikipediaLanguage lang) {
        return replacementTypeRepository.countReplacementsByType(lang);
    }
}
