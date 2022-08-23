package es.bvalero.replacer.page.count;

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

@Tag(name = "Pages")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api")
public class PageCountController {

    @Autowired
    private PageCountService pageCountService;

    @Operation(summary = "Count the pages to review grouped by type (kind-subtype)")
    @GetMapping(value = "/page/type/count")
    public Collection<KindCount> countReplacementsGroupedByType(@Valid CommonQueryParameters queryParameters) {
        return pageCountService.countReplacementsGroupedByType(
            queryParameters.getWikipediaLanguage(),
            queryParameters.getUser()
        );
    }
}
