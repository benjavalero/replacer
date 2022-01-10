package es.bvalero.replacer.replacement.count;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
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
    private ReplacementCountService replacementCountService;

    @Operation(
        summary = "List replacement types with the number of pages containing replacements of these types to review"
    )
    @GetMapping(value = "/replacement-types/count")
    public Collection<TypeCount> countReplacementsGroupedByType(@Valid CommonQueryParameters queryParameters) {
        return replacementCountService.countReplacementsGroupedByType(queryParameters.getWikipediaLanguage());
    }
}
