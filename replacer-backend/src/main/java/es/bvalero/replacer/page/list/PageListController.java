package es.bvalero.replacer.page.list;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.authentication.userrights.CheckUserRightsService;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.exception.ForbiddenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Pages")
@Loggable(skipResult = true)
@RestController
@RequestMapping("api/pages")
public class PageListController {

    @Autowired
    private CheckUserRightsService checkUserRightsService;

    @Autowired
    private PageUnreviewedTitleListService pageUnreviewedTitleListService;

    @Autowired
    private ReviewByTypeService reviewByTypeService;

    @Operation(
        summary = "Produce a list in plain text with the titles of the pages containing the given replacement type to review"
    )
    @GetMapping(value = "", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPageTitlesToReviewByType(
        @Valid CommonQueryParameters queryParameters,
        @Valid PageListRequest request
    ) {
        validateBotUser(queryParameters.getWikipediaLanguage(), queryParameters.getUser());

        String titleList = StringUtils.join(
            pageUnreviewedTitleListService.findPageTitlesToReviewByType(
                queryParameters.getWikipediaLanguage(),
                toDomain(request)
            ),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @Operation(summary = "Mark as reviewed by the system all pages pages containing the given replacement type")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/review")
    public void reviewAsSystemByType(@Valid CommonQueryParameters queryParameters, @Valid PageListRequest request) {
        validateBotUser(queryParameters.getWikipediaLanguage(), queryParameters.getUser());

        // Set as reviewed in the database
        reviewByTypeService.reviewAsSystemByType(queryParameters.getWikipediaLanguage(), toDomain(request));
    }

    private ReplacementType toDomain(PageListRequest request) {
        return ReplacementType.of(request.getType(), request.getSubtype());
    }

    private void validateBotUser(WikipediaLanguage lang, String user) throws ForbiddenException {
        if (!checkUserRightsService.isBot(lang, user)) {
            LOGGER.error("Unauthorized bot user: {} - {}", lang, user);
            throw new ForbiddenException();
        }
    }
}
