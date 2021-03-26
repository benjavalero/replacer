package es.bvalero.replacer.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class PageSection {

    @ApiModelProperty(value = "Section ID content", required = true, example = "1")
    Integer id;

    @ApiModelProperty(value = "Section title", required = true, example = "Biografía")
    String title;

    static PageSection of(Integer id, String title) {
        return new PageSection(id, title);
    }
}
