package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Implementation of the replacement repository which maintains a cache of the replacement counts */
@Primary
@Component
@Qualifier("replacementCacheRepository")
class ReplacementCacheRepository implements ReplacementRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementRepository replacementRepository;

    @Override
    public void insertReplacements(Collection<ReplacementModel> replacements) {
        replacementRepository.insertReplacements(replacements);
    }

    @Override
    public void updateReplacements(Collection<ReplacementModel> replacements) {
        replacementRepository.updateReplacements(replacements);
    }

    @Override
    public void deleteReplacements(Collection<ReplacementModel> replacements) {
        replacementRepository.deleteReplacements(replacements);
    }

    @Override
    public void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        // TODO: Use cache implementation
        replacementRepository.reviewAsSystemByType(lang, type, subtype);
    }
}
