package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@ApiModel(description = "Section of a page to review")
@Data
@NoArgsConstructor
public class ReviewSection {

    // Public: it is an in/out DTO

    @ApiModelProperty(value = "Section ID", required = true, example = "1")
    @NonNull
    Integer id;

    @ApiModelProperty(value = "Section title", required = true, example = "Biografía")
    @NonNull
    String title;
}
