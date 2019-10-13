package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
class WikipediaApiResponse {
    private Error error;
    private boolean batchcomplete;
    private String curtimestamp;
    @JsonProperty("continue")
    private Continue continueObj;
    private Query query;
    private Page parse;

    @Data
    static class Error {
        private String code;
        private String info;
    }

    @Data
    static class Continue {
        private int sroffset;
        @JsonProperty("continue")
        private String continueStr;
    }

    @Data
    static class Query {
        private UserInfo userinfo;
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
    static class Page {
        private int pageid;
        private int ns;
        private String title;
        private List<Revision> revisions;
        private List<Section> sections;
        private boolean missing;
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
    static class Main {
        private String contentmodel;
        private String contentformat;
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
