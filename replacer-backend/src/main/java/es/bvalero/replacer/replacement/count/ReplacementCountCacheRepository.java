package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Implementation of the replacement repository which maintains a cache of the replacement counts */
@Primary
@Component
@Qualifier("replacementCountCacheRepository")
class ReplacementCountCacheRepository implements ReplacementCountRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementCountRepository replacementCountRepository;

    @Override
    public void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        // TODO: Use cache implementation
        replacementCountRepository.reviewAsSystemByType(lang, type, subtype);
    }
}
