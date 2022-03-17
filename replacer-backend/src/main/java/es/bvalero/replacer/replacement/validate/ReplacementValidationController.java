package es.bvalero.replacer.replacement.validate;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@Tag(name = "Replacements")
@Loggable(entered = true)
@RestController
@RequestMapping("api/replacement")
public class ReplacementValidationController {

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Operation(summary = "Validate if the custom replacement matches with a known replacement type")
    @GetMapping(value = "/type/validate")
    public ResponseEntity<ReplacementTypeDto> validateCustomReplacement(
        @Valid CommonQueryParameters queryParameters,
        @Valid ReplacementValidationRequest validationRequest
    ) {
        Optional<ReplacementType> type = replacementFinderService.findMatchingReplacementType(
            queryParameters.getWikipediaLanguage(),
            validationRequest.getReplacement(),
            validationRequest.isCs()
        );
        if (type.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(ReplacementTypeDto.fromDomain(type.get()));
        }
    }
}
