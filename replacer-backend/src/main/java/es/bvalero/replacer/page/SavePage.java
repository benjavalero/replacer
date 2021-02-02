package es.bvalero.replacer.page;

import es.bvalero.replacer.wikipedia.AccessToken;
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
    private String token;
    private String tokenSecret;

    @Nullable
    private String type;

    @Nullable
    private String subtype;

    AccessToken getAccessToken() {
        return AccessToken.of(token, tokenSecret);
    }

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
