package es.bvalero.replacer.replacement;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import es.bvalero.replacer.finder.ReplacementTypeMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Replacements")
@Loggable(entered = true)
@RestController
@RequestMapping("api/replacement")
public class ReplacementValidationController {

    @Autowired
    private ReplacementTypeMatchService replacementTypeMatchService;

    @Operation(summary = "Validate if the custom replacement matches with a known replacement type")
    @GetMapping(value = "/type/validate")
    public ResponseEntity<ReplacementTypeDto> validateCustomReplacement(
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementValidationRequest validationRequest
    ) {
        Optional<ReplacementType> type = replacementTypeMatchService.findMatchingReplacementType(
            queryParameters.getWikipediaLanguage(),
            validationRequest.getReplacement(),
            validationRequest.isCs()
        );
        return type
            .map(replacementType -> ResponseEntity.ok(ReplacementTypeDto.of(replacementType)))
            .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
