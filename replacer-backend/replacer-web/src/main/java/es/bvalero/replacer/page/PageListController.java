package es.bvalero.replacer.page;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.user.ValidateBotUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
    private ReplacementService replacementService;

    @Operation(summary = "List the pages to review containing the given replacement type")
    @ValidateBotUser
    @GetMapping(value = "/type", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> findPagesToReviewByType(
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE) String langHeader,
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementTypeDto request
    ) {
        StandardType type = request.toDomain();
        String titleList = StringUtils.join(
            pageFindByTypeService.findPagesToReviewByType(WikipediaLanguage.valueOfCode(langHeader), type),
            "\n"
        );
        return new ResponseEntity<>(titleList, HttpStatus.OK);
    }

    @Operation(summary = "Mark as reviewed the pages containing the given replacement type")
    @ValidateBotUser
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/type/review")
    public void reviewPagesByType(
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE) String langHeader,
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementTypeDto request
    ) {
        StandardType type = request.toDomain();
        replacementService.reviewReplacementsByType(WikipediaLanguage.valueOfCode(langHeader), type);
    }
}
