package es.bvalero.replacer.finder.benchmark.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikipediaApiResponse {

    // We cannot reuse the class in wikipedia submodule as submodules cannot access each other

    private Query query;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {

        private Collection<Page> pages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {

        private int pageid;

        private int ns;

        private String title;

        private Collection<Revision> revisions;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Revision {

        private String timestamp;
        private Slots slots;
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
}
