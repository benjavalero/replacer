package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.List;
import lombok.Data;
import org.springframework.lang.Nullable;

/** DTO matching the common format for a Wikipedia classic API response */
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
    static class Query {

        private UserInfo userinfo;
        private SearchInfo searchinfo;
        private List<Page> search;
        private List<Page> pages;
        private Tokens tokens;
        private List<User> users;
    }

    @Data
    static class UserInfo {

        private int id;
        private String name;
        private List<String> groups;
    }

    @Data
    static class SearchInfo {

        private int totalhits;
    }

    @Data
    static class Page {

        private String contentmodel;
        private int lastrevid;
        private int length;
        private int pageid;
        private int ns;
        private String pagelanguage;
        private String pagelanguagedir;
        private String pagelanguagehtmlcode;
        private List<Protection> protection;
        private boolean redirect;
        private List<String> restrictiontypes;
        private String title;
        private List<Revision> revisions;
        private List<Section> sections;
        private String touched;
        private boolean missing;
        private boolean showtoc;

        @JsonProperty("new")
        private boolean newPage;

        boolean isProtected() {
            return this.protection != null && this.protection.stream().anyMatch(Protection::isLibrarianEditProtection);
        }
    }

    @Data
    private static class Protection {

        private String expiry;
        private String level;
        private String type;

        boolean isLibrarianEditProtection() {
            return "edit".equals(type) && "sysop".equals(level);
        }
    }

    @Data
    static class Revision {

        private String timestamp;
        private Slots slots;
    }

    @Data
    static class Tokens {

        private String csrftoken;
    }

    @Data
    static class Slots {

        private Main main;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Main {

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
        private String linkAnchor;

        @JsonProperty("html-summary")
        private String htmlSummary;
    }

    @Data
    static class User {

        private int userid;
        private String name;
        private List<String> groups;
    }
}
