package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import es.bvalero.replacer.ReplacerException;
import java.util.List;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class WikipediaApiResponse {

    @Nullable
    private Error error;

    private boolean batchcomplete;
    private String curtimestamp;
    private Query query;
    private Page parse;

    /**
     * @throws ReplacerException if the response contains an error.
     */
    void validate() throws ReplacerException {
        if (this.error != null) {
            String code = this.error.getCode();
            String info = this.error.getInfo();
            throw new ReplacerException(String.format("%s: %s", code, info));
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Error {

        private String code;
        private String info;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {

        private UserInfo userinfo;
        private SearchInfo searchinfo;
        private List<Page> search;
        private List<Page> pages;
        private Tokens tokens;
    }

    @Data
    static class UserInfo {

        private int id;
        private String name;
    }

    @Data
    static class SearchInfo {

        private int totalhits;
    }

    @Data
    public static class Page {

        private int pageid;
        private int ns;
        private String title;
        private List<Revision> revisions;
        private List<Section> sections;
        private boolean missing;
    }

    @Data
    public static class Revision {

        private String timestamp;
        private Slots slots;
    }

    @Data
    static class Tokens {

        private String csrftoken;
    }

    @Data
    public static class Slots {

        private Main main;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {

        private String content;
    }

    @Data
    static class Section {

        private int toclevel;
        private String level;
        private String line;
        private String number;
        private String index;
        private String fromtitle;
        private Integer byteoffset;
        private String anchor;
    }
}
