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

    Collection<TypeCount> countReplacementsGroupedByType(WikipediaLanguage lang) {
        return toDto(replacementTypeRepository.countReplacementsByType(lang));
    }

    private Collection<TypeCount> toDto(Collection<ResultCount<ReplacementType>> counts) {
        final Map<String, TypeCount> typeCounts = new TreeMap<>();
        for (ResultCount<ReplacementType> count : counts) {
            String type = count.getKey().getKind().getLabel();
            TypeCount typeCount = typeCounts.computeIfAbsent(type, TypeCount::of);
            typeCount.add(SubtypeCount.of(count.getKey().getSubtype(), count.getCount()));
        }
        return typeCounts.values().stream().sorted().collect(Collectors.toUnmodifiableList());
    }
}
