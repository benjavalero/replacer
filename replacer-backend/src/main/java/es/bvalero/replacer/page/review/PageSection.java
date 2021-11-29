package es.bvalero.replacer.page.review;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSection {

    // TODO: Public while refactoring

    @ApiModelProperty(value = "Section ID content", required = true, example = "1")
    Integer id;

    @ApiModelProperty(value = "Section title", required = true, example = "Biografía")
    String title;

    // TODO: Public while refactoring
    public static PageSection of(Integer id, String title) {
        return new PageSection(id, title);
    }
}
