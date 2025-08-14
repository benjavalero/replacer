package es.bvalero.replacer.page.count;

import static java.util.stream.Collectors.*;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.resolver.AuthenticatedUser;
import es.bvalero.replacer.finder.StandardType;
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
    private final PageCountApi pageCountApi;

    PageCountController(PageCountApi pageCountApi) {
        this.pageCountApi = pageCountApi;
    }

    @Operation(summary = "Count the number of pages to review grouped by replacement type")
    @GetMapping(value = "/type/count")
    Collection<KindCount> countNotReviewedGroupedByType(@AuthenticatedUser User user) {
        Collection<KindCount> counts = pageCountApi
            .countNotReviewedGroupedByType(user)
            .stream()
            .collect(groupingBy(rc -> rc.getKey().getKind(), mapping(this::mapResultCount, toList())))
            .entrySet()
            .stream()
            .map(entry -> KindCount.of(entry.getKey().getCode(), entry.getValue()))
            .sorted()
            .toList();
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
