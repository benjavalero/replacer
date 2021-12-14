package es.bvalero.replacer.replacement.count;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.exception.ReplacerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
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
    private ReplacementCacheRepository replacementCacheRepository;

    @Operation(
        summary = "List replacement types with the number of pages containing replacements of these types to review"
    )
    @GetMapping(value = "/replacement-types/count")
    public Collection<TypeCount> countReplacementsGroupedByType(@Valid CommonQueryParameters queryParameters)
        throws ReplacerException {
        // Repositories should never be called in a Controller
        // In this case we make an exception as we are actually calling the cached implementation
        return replacementCacheRepository.countReplacementsGroupedByType(queryParameters.getWikipediaLanguage());
    }
}
