package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.List;
import lombok.Data;

/** DTO matching the common format for a Wikipedia classic API response */
@Data
public class WikipediaApiResponse {

    private Error error;

    private boolean batchcomplete;

    @JsonProperty("continue")
    private Continue continueObject;

    private String curtimestamp;
    private Query query;
    private Page parse;
    private Edit edit;
    private String servedby;

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
    private static class Error {

        private String code;
        private String info;
        private String docref;
    }

    @Data
    private static class Continue {

        private int sroffset;

        @JsonProperty("continue")
        private String continueValue;
    }

    @Data
    public static class Query {

        private UserInfo userinfo;
        private SearchInfo searchinfo;
        private List<Normalized> normalized;
        private List<Page> search;
        private List<Page> pages;
        private Tokens tokens;
        private List<User> users;
    }

    @Data
    public static class UserInfo {

        private int id;
        private String name;
        private List<String> groups;
    }

    @Data
    public static class SearchInfo {

        private int totalhits;
    }

    @Data
    private static class Normalized {

        boolean fromencoded;
        String from;
        String to;
    }

    @Data
    public static class Page {

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

        public boolean isProtected() {
            return this.protection != null && this.protection.stream().anyMatch(Protection::isLibrarianEditProtection);
        }
    }

    @Data
    private static class Protection {

        private String expiry;
        private String level;
        private String type;

        boolean isLibrarianEditProtection() {
            return "edit".equals(this.type) && "sysop".equals(this.level);
        }
    }

    @Data
    public static class Revision {

        private String timestamp;
        private Slots slots;
    }

    @Data
    public static class Tokens {

        private String csrftoken;
    }

    @Data
    public static class Slots {

        private Main main;
    }

    @Data
    public static class Main {

        private String contentmodel;
        private String contentformat;
        private String content;
    }

    @Data
    public static class Section {

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
    public static class User {

        private int userid;
        private String name;
        private boolean missing;
        private List<String> groups;
    }

    @Data
    private static class Edit {

        private String result;
        private int pageid;
        private String title;
        private String contentmodel;
        private int oldrevid;
        private int newrevid;
        private String newtimestamp;
    }
}
