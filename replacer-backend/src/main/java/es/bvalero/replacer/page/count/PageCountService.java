package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageCountService {

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

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
