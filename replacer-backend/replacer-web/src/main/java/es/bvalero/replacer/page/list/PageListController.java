package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import es.bvalero.replacer.common.resolver.UserLanguage;
import es.bvalero.replacer.common.security.ValidateBotUser;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Page")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/page/type")
class PageListController {

    // Dependency injection
    private final PageListApi pageListApi;

    PageListController(PageListApi pageListApi) {
        this.pageListApi = pageListApi;
    }

    @Operation(summary = "List the pages to review containing the given replacement type")
    @GetMapping(value = "", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> findPageTitlesNotReviewedByType(
        @RequestParam String lang,
        @Valid ReplacementTypeDto request
    ) {
        // We cannot use the ValidateBotUser annotation because this call is made in an external tab
        WikipediaLanguage language = WikipediaLanguage.valueOfCode(lang);
        StandardType type = request.toStandardType();
        Collection<String> pagesToReview = pageListApi.findPageTitlesNotReviewedByType(language, type);
        String titleList = StringUtils.join(pagesToReview, "\n");
        LOGGER.info(
            "GET List of Pages to Review by Type: {} => {} items",
            ReplacerUtils.toJson("lang", language, "type", request),
            pagesToReview.size()
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @Operation(summary = "Mark as reviewed the pages containing the given replacement type")
    @ValidateBotUser
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "")
    void reviewPagesByType(@UserLanguage WikipediaLanguage lang, @Valid ReplacementTypeDto request) {
        LOGGER.info("POST Review pages by type {}", request);
        StandardType type = request.toStandardType();
        pageListApi.updateSystemReviewerByType(lang, type);
    }
}
