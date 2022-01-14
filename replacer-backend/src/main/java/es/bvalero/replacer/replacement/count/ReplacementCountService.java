package es.bvalero.replacer.replacement.count;

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
class ReplacementCountService {

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    Collection<KindCount> countReplacementsGroupedByType(WikipediaLanguage lang) {
        return toDto(replacementTypeRepository.countReplacementsByType(lang));
    }

    private Collection<KindCount> toDto(Collection<ResultCount<ReplacementType>> counts) {
        final Map<Byte, KindCount> kindCounts = new TreeMap<>();
        for (ResultCount<ReplacementType> count : counts) {
            byte kindCode = count.getKey().getKind().getCode();
            KindCount kindCount = kindCounts.computeIfAbsent(
                kindCode,
                k -> KindCount.of(count.getKey().getKind().getCode())
            );
            kindCount.add(TypeCount.of(count.getKey().getSubtype(), count.getCount()));
        }
        return kindCounts.values().stream().sorted().collect(Collectors.toUnmodifiableList());
    }
}
