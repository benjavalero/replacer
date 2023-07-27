package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import es.bvalero.replacer.finder.ReplacementTypeMatchService;
import es.bvalero.replacer.user.UserLanguage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Replacement")
@Slf4j
@RestController
@RequestMapping("api/replacement")
public class ReplacementValidationController {

    @Autowired
    private ReplacementTypeMatchService replacementTypeMatchService;

    @Operation(summary = "Validate if the custom replacement matches with a known replacement type")
    @GetMapping(value = "/type/validate")
    public ResponseEntity<ReplacementTypeDto> validateCustomReplacement(
        @UserLanguage WikipediaLanguage lang,
        @Valid ReplacementValidationRequest validationRequest
    ) {
        Optional<StandardType> type = replacementTypeMatchService.findMatchingReplacementType(
            lang,
            validationRequest.getReplacement(),
            validationRequest.isCs()
        );
        if (type.isPresent()) {
            ReplacementTypeDto typeDto = ReplacementTypeDto.of(type.get());
            LOGGER.info("GET Validate Custom Replacement: {} => {}", validationRequest, typeDto);
            return ResponseEntity.ok(typeDto);
        } else {
            LOGGER.info("GET Validate Custom Replacement: {} => {}", validationRequest, "No Content");
            return ResponseEntity.noContent().build();
        }
    }
}
