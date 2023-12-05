package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.dto.ReplacementTypeDto;
import es.bvalero.replacer.common.resolver.UserLanguage;
import es.bvalero.replacer.finder.ReplacementTypeFindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Replacement Type")
@Slf4j
@PrimaryAdapter
@RestController
@RequestMapping("api/type")
class ReplacementTypeController {

    // Dependency injection
    private final ReplacementTypeFindService replacementTypeFindService;

    ReplacementTypeController(ReplacementTypeFindService replacementTypeFindService) {
        this.replacementTypeFindService = replacementTypeFindService;
    }

    @Operation(summary = "Validate if the custom replacement matches with a known replacement type")
    @GetMapping(value = "/validate")
    ResponseEntity<ReplacementTypeDto> validateCustomReplacement(
        @UserLanguage WikipediaLanguage lang,
        @Valid ReplacementTypeValidationRequest validationRequest
    ) {
        Optional<StandardType> type = replacementTypeFindService.findReplacementType(
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
