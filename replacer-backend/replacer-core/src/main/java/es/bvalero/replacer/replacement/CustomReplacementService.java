package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementService {

    @Autowired
    private CustomRepository customRepository;

    public void addCustomReplacement(IndexedCustomReplacement customReplacement) {
        customRepository.add(customReplacement);
    }

    public Collection<Integer> findPagesReviewed(WikipediaLanguage lang, CustomType type) {
        return customRepository.findPagesReviewed(lang, type).stream().map(PageKey::getPageId).toList();
    }
}
