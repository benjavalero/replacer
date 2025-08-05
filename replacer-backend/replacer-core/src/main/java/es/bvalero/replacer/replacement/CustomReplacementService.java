package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomType;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementService {

    // Dependency injection
    private final CustomRepository customRepository;

    public CustomReplacementService(CustomRepository customRepository) {
        this.customRepository = customRepository;
    }

    // TODO: Not worth a single service only for this method
    public Collection<Integer> findPagesReviewed(WikipediaLanguage lang, CustomType type) {
        return customRepository.findPagesReviewed(lang, type).stream().map(PageKey::getPageId).toList();
    }
}
