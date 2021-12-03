package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.page.repository.ReplacementRepository;
import es.bvalero.replacer.replacement.ReplacementService;
import java.text.Collator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageListService {

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private ReplacementRepository replacementRepository;

    List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        return replacementService
            .findPageTitlesToReviewBySubtype(lang, type, subtype)
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(FinderUtils.LOCALE_ES))
            .collect(Collectors.toList());
    }

    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        // These reviewed replacements will be cleaned up in the next dump indexing
        replacementRepository.reviewAsSystemByType(lang, type, subtype);
    }
}
