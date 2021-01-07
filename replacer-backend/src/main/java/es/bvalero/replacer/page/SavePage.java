package es.bvalero.replacer.page;

import es.bvalero.replacer.authentication.AccessToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
class SavePage {

    @Nullable
    private Integer section;

    private String content;
    private String timestamp;
    private String reviewer;
    private AccessToken token;

    @Nullable
    private String type;

    @Nullable
    private String subtype;
}
