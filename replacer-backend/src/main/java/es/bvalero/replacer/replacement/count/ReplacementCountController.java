package es.bvalero.replacer.replacement.count;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.repository.ResultCount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Replacements")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api")
public class ReplacementCountController {

    @Autowired
    private ReplacementCountService replacementCountService;

    @Operation(
        summary = "List replacement types with the number of pages containing replacements of these types to review"
    )
    @GetMapping(value = "/replacement-types/count")
    public Collection<TypeCount> countReplacementsGroupedByType(@Valid CommonQueryParameters queryParameters) {
        return toDto(replacementCountService.countReplacementsGroupedByType(queryParameters.getWikipediaLanguage()));
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
