package es.bvalero.replacer.replacement.count;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "replacements")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api")
public class ReplacementCountController {

    @Autowired
    private ReplacementCacheRepository replacementCacheRepository;

    @ApiOperation(
        value = "List replacement types with the number of pages containing replacement of these types to review"
    )
    @GetMapping(value = "/replacement-types/count")
    public Collection<TypeCount> countReplacementsGroupedByType(
        @ApiParam(value = "Language", allowableValues = "es, gl", required = true) @RequestParam WikipediaLanguage lang
    ) throws ReplacerException {
        // Repositories should never be called in a Controller
        // In this case we make an exception as we are actually calling the cached implementation
        return replacementCacheRepository.countReplacementsGroupedByType(lang);
    }
}
