package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.Collection;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * DTO to represent any possible response from Wikipedia API,
 * ot at least the fields we are interested in, ignoring the rest.
 * Note there is no fixed schema to follow up the changes in the API specification.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikipediaApiResponse {

    @Nullable
    private Error error;

    private boolean batchcomplete;

    private String curtimestamp;

    private Query query;

    private Parse parse;

    /**
     * @throws WikipediaException if the response contains an error.
     */
    void validate() throws WikipediaException {
        if (this.error != null) {
            String code = this.error.getCode();
            String info = this.error.getInfo();
            throw new WikipediaException(String.format("%s: %s", code, info));
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Error {

        private String code;
        private String info;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {

        private UserInfo userinfo;

        private Collection<User> users;

        private Collection<Page> pages;

        private SearchInfo searchinfo;

        private Collection<Search> search;

        private Tokens tokens;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserInfo {

        private String name;
        private Collection<String> groups;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchInfo {

        private int totalhits;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Search {

        private int pageid;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {

        private int pageid;

        private int ns;

        private boolean missing;

        private String title;

        private boolean redirect;

        @Nullable
        private Collection<Protection> protection;

        private Collection<Revision> revisions;

        public boolean isProtected() {
            return this.protection != null && this.protection.stream().anyMatch(Protection::isLibrarianEditProtection);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Protection {

        private String type;
        private String level;

        private boolean isLibrarianEditProtection() {
            return "edit".equals(this.type) && "sysop".equals(this.level);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Revision {

        private String timestamp;

        private Slots slots;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tokens {

        private String csrftoken;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Slots {

        private Main main;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {

        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parse {

        private Collection<Section> sections;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section {

        private String level;

        private String index; // It can be blank

        @Nullable
        private Integer byteoffset;

        private String anchor;
        private String linkAnchor;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {

        private String name;
        private boolean missing;
        private Collection<String> groups;
    }
}
