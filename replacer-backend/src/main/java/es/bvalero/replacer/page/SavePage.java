package es.bvalero.replacer.page;

import es.bvalero.replacer.authentication.AccessToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
class SavePage {

    @Nullable
    private Integer section;

    private String title;
    private String content;
    private String timestamp;
    private AccessToken token;

    @Nullable
    private String type;

    @Nullable
    private String subtype;

    @Override
    public String toString() {
        return (
            "SavePage(section=" +
            this.getSection() +
            ", title=" +
            this.getTitle() +
            ", content=" +
            StringUtils.abbreviate(this.getContent(), PageController.CONTENT_SIZE) +
            ", type=" +
            this.getType() +
            ", subtype=" +
            this.getSubtype() +
            ")"
        );
    }
}
