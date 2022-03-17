package es.bvalero.replacer.page.list;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import es.bvalero.replacer.user.ValidateBotUser;
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
@RequestMapping("api/page")
public class PageListController {

    @Autowired
    private PageFindByTypeService pageFindByTypeService;

    @Autowired
    private PageReviewByTypeService pageReviewByTypeService;

    @Operation(summary = "List the pages to review containing the given replacement type")
    @ValidateBotUser
    @GetMapping(value = "/type", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPagesToReviewByType(
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementTypeDto request
    ) {
        String titleList = StringUtils.join(
            pageFindByTypeService.findPagesToReviewByType(queryParameters.getWikipediaLanguage(), request.toDomain()),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @Operation(summary = "Mark as reviewed the pages containing the given replacement type")
    @ValidateBotUser
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/type/review")
    public void reviewPagesByType(@Valid CommonQueryParameters queryParameters, @Valid ReplacementTypeDto request) {
        pageReviewByTypeService.reviewPagesByType(queryParameters.getWikipediaLanguage(), request.toDomain());
    }
}
