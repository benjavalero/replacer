package es.bvalero.replacer.article;

import es.bvalero.replacer.authentication.AccessToken;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
class SaveArticle {
    private int articleId;
    private @Nullable Integer section;
    private String content;
    private String timestamp;
    private String reviewer;
    private AccessToken token;
    private @Nullable String type;
    private @Nullable String subtype;
}
