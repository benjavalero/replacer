package es.bvalero.replacer.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Schema(description = "Section of a page to review")
@Data
@NoArgsConstructor
public class ReviewSectionDto {

    @Schema(description = "Section ID", required = true, example = "1")
    @NotNull
    int id;

    @Schema(description = "Section title", required = true, example = "Biografía")
    @NonNull
    @NotNull
    String title;
}
