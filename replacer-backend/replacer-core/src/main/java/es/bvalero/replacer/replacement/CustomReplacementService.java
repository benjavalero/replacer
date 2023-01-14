package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomReplacementService {

    @Autowired
    private CustomRepository customRepository;

    public void addCustomReplacement(IndexedCustomReplacement customReplacement) {
        customRepository.add(customReplacement);
    }

    public Collection<Integer> findPagesReviewed(WikipediaLanguage lang, String replacement, boolean caseSensitive) {
        return customRepository
            .findPagesReviewed(lang, replacement, caseSensitive)
            .stream()
            .map(PageKey::getPageId)
            .collect(Collectors.toUnmodifiableList());
    }
}
