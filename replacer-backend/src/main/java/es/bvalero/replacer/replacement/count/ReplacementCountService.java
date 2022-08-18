package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementCountRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to retrieve and cache the replacement counts for statistics */
@Service
class ReplacementCountService {

    @VisibleForTesting
    static final int NUM_RESULTS = 20;

    @Autowired
    private ReplacementCountRepository replacementCountRepository;

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    int countReplacementsReviewed(WikipediaLanguage lang) {
        return this.replacementCountRepository.countReplacementsReviewed(lang);
    }

    int countReplacementsNotReviewed(WikipediaLanguage lang) {
        return this.replacementCountRepository.countReplacementsNotReviewed(lang);
    }

    Collection<ResultCount<String>> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        return this.replacementCountRepository.countReplacementsByReviewer(lang);
    }

    Collection<ResultCount<PageModel>> countReplacementsGroupedByPage(WikipediaLanguage lang) {
        return replacementCountRepository.countReplacementsByPage(lang, NUM_RESULTS);
    }

    Collection<KindCount> countReplacementsGroupedByType(WikipediaLanguage lang) {
        return toDto(replacementTypeRepository.countReplacementsByType(lang));
    }

    // This mapping from domain to DTO could be done in the Controller instead
    // We do it here to keep the Controller simpler
    private Collection<KindCount> toDto(Collection<ResultCount<ReplacementType>> counts) {
        final Map<Byte, KindCount> kindCounts = new TreeMap<>();
        for (ResultCount<ReplacementType> count : counts) {
            byte kindCode = count.getKey().getKind().getCode();
            KindCount kindCount = kindCounts.computeIfAbsent(
                kindCode,
                k -> KindCount.of(count.getKey().getKind().getCode())
            );
            kindCount.add(SubtypeCount.of(count.getKey().getSubtype(), count.getCount()));
        }

        // Sort the collection by kind
        return kindCounts.values().stream().sorted().collect(Collectors.toUnmodifiableList());
    }
}
