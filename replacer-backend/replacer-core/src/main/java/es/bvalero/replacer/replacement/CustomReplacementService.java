package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementService {

    // Dependency injection
    private final CustomRepository customRepository;

    public CustomReplacementService(CustomRepository customRepository) {
        this.customRepository = customRepository;
    }

    public void addCustomReplacement(IndexedCustomReplacement customReplacement) {
        customRepository.add(customReplacement);
    }

    public Collection<Integer> findPagesReviewed(WikipediaLanguage lang, CustomType type) {
        return customRepository.findPagesReviewed(lang, type).stream().map(PageKey::getPageId).toList();
    }
}
