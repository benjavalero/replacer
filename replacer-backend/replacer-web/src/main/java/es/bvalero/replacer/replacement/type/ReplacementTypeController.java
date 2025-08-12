package es.bvalero.replacer.replacement.type;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.resolver.UserLanguage;
import es.bvalero.replacer.finder.StandardType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
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
    private final ReplacementTypeFindApi replacementTypeFindApi;

    ReplacementTypeController(ReplacementTypeFindApi replacementTypeFindApi) {
        this.replacementTypeFindApi = replacementTypeFindApi;
    }

    @Operation(summary = "Find a known standard type matching with the given replacement and case-sensitive option")
    @GetMapping(value = "")
    ResponseEntity<ReplacementTypeDto> findReplacementType(
        @UserLanguage WikipediaLanguage lang,
        @Valid ReplacementTypeFindRequest replacementTypeFindRequest
    ) {
        Optional<StandardType> type = replacementTypeFindApi.findReplacementType(
            lang,
            replacementTypeFindRequest.getReplacement(),
            replacementTypeFindRequest.isCs()
        );
        if (type.isPresent()) {
            ReplacementTypeDto typeDto = toDto(type.get());
            LOGGER.info("GET Find Replacement Type: {} => {}", replacementTypeFindRequest, typeDto);
            return ResponseEntity.ok(typeDto);
        } else {
            LOGGER.info("GET Find Replacement Type: {} => {}", replacementTypeFindRequest, "No Content");
            return ResponseEntity.noContent().build();
        }
    }

    private ReplacementTypeDto toDto(StandardType type) {
        return ReplacementTypeDto.of(type.getKind().getCode(), type.getSubtype());
    }
}
