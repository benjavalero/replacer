package es.bvalero.replacer.page;

import static java.util.stream.Collectors.*;

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
@RequestMapping("api/page")
public class PageCountController {

    @Autowired
    private PageCountService pageCountService;

    @Operation(summary = "Count the pages to review grouped by type (kind-subtype)")
    @GetMapping(value = "/type/count")
    public Collection<KindCount> countPagesNotReviewedByType(@Valid CommonQueryParameters queryParameters) {
        return pageCountService
            .countPagesNotReviewedByType(queryParameters.getUserId())
            .stream()
            .collect(
                groupingBy(
                    rc -> rc.getKey().getKind(),
                    mapping(rc -> SubtypeCount.of(rc.getKey().getSubtype(), rc.getCount()), toList())
                )
            )
            .entrySet()
            .stream()
            .map(entry -> KindCount.of(entry.getKey().getCode(), entry.getValue()))
            .sorted()
            .collect(toList());
    }
}
