package es.bvalero.replacer.page.count;

import static java.util.stream.Collectors.*;

import es.bvalero.replacer.user.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Pages")
@Slf4j
@RestController
@RequestMapping("api/page")
public class PageCountController {

    @Autowired
    private PageCountService pageCountService;

    @Operation(summary = "Count the number of pages to review grouped by replacement type")
    @GetMapping(value = "/type/count")
    public Collection<KindCount> countNotReviewedGroupedByType(@AuthenticatedUser User user) {
        Collection<KindCount> counts = pageCountService
            .countNotReviewedGroupedByType(user)
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
        LOGGER.info("GET Count Pages Not Reviewed By Type: {}", counts);
        return counts;
    }
}
