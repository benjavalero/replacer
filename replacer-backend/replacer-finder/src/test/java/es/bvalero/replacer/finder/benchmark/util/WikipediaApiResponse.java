package es.bvalero.replacer.finder.benchmark.util;

import java.util.List;
import lombok.Data;

@Data
class WikipediaApiResponse {

    // We cannot reuse the class in wikipedia submodule as submodules cannot access each other

    private boolean batchcomplete;
    private String curtimestamp;
    private Query query;

    @Data
    static class Query {

        private List<Page> pages;
    }

    @Data
    static class Page {

        private int pageid;
        private int ns;
        private String title;
        private List<Revision> revisions;
    }

    @Data
    static class Revision {

        private String timestamp;
        private Slots slots;
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
}
