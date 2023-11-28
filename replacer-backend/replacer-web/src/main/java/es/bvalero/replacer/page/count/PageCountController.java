package es.bvalero.replacer.page.count;

import static java.util.stream.Collectors.*;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.util.AuthenticatedUser;
import es.bvalero.replacer.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Page")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/page")
class PageCountController {

    // Dependency injection
    private final PageCountService pageCountService;

    PageCountController(PageCountService pageCountService) {
        this.pageCountService = pageCountService;
    }

    @Operation(summary = "Count the number of pages to review grouped by replacement type")
    @GetMapping(value = "/type/count")
    Collection<KindCount> countNotReviewedGroupedByType(@AuthenticatedUser User user) {
        Collection<KindCount> counts = pageCountService
            .countNotReviewedGroupedByType(user)
            .stream()
            .collect(groupingBy(rc -> rc.getKey().getKind(), mapping(this::mapResultCount, toList())))
            .entrySet()
            .stream()
            .map(entry -> KindCount.of(entry.getKey().getCode(), entry.getValue()))
            .sorted()
            .collect(toList());
        LOGGER.info("GET Count Pages Not Reviewed By Type: {}", counts);
        return counts;
    }

    private SubtypeCount mapResultCount(ResultCount<StandardType> rc) {
        StandardType type = rc.getKey();
        Boolean forBots = type.isForBots() ? true : null;
        Boolean forAdmin = type.isForAdmin() ? true : null;
        return SubtypeCount.of(rc.getKey().getSubtype(), rc.getCount(), forBots, forAdmin);
    }
}
