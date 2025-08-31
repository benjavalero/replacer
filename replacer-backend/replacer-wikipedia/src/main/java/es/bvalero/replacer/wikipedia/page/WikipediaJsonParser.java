package es.bvalero.replacer.wikipedia.page;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.wikipedia.WikipediaNamespace;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Stream;
import lombok.SneakyThrows;

/**
 * Handler to parse a Wikipedia Page response in JSON
 */
public class WikipediaJsonParser {

    public static Stream<WikipediaPage> parse(WikipediaLanguage lang, InputStream jsonResponse) {
        Iterable<WikipediaPage> pageIterable = () -> new WikipediaJsonIterator(lang, jsonResponse);
        return ReplacerUtils.streamOfIterable(pageIterable);
    }

    private static class WikipediaJsonIterator implements Iterator<WikipediaPage> {

        private static final String CURTIMESTAMP_TAG = "curtimestamp";
        private static final String QUERY_TAG = "query";
        private static final String PAGES_TAG = "pages";
        private static final String ID_TAG = "pageid";
        private static final String NAMESPACE_TAG = "ns";
        private static final String MISSING_TAG = "missing";
        private static final String TITLE_TAG = "title";
        private static final String REDIRECT_TAG = "redirect";
        private static final String PROTECTION_TAG = "protection";
        private static final String TYPE_TAG = "type";
        private static final String LEVEL_TAG = "level";
        private static final String REVISIONS_TAG = "revisions";
        private static final String TIMESTAMP_TAG = "timestamp";
        private static final String SLOTS_TAG = "slots";
        private static final String MAIN_TAG = "main";
        private static final String CONTENT_TAG = "content";

        private final WikipediaLanguage lang;
        private final JsonParser parser;

        // Current page values
        private String currentCurTimestamp;
        private String currentTitle;
        private int currentNamespace;
        private int currentId;
        private String currentTimestamp;
        private String currentContent;
        private boolean currentMissing;
        private boolean currentRedirect;
        private boolean currentProtected;

        @SneakyThrows
        WikipediaJsonIterator(WikipediaLanguage lang, InputStream jsonResponse) {
            this.lang = lang;
            this.parser = new JsonFactory().createParser(jsonResponse);
            advanceToPagesArray();
        }

        private void advanceToPagesArray() throws Exception {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new Exception("Expected a JSON object at the start");
            }

            // Advance until the end of the main object
            // or the start of the page array
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                switch (fieldName) {
                    case CURTIMESTAMP_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentCurTimestamp = parser.getValueAsString();
                    }
                    case QUERY_TAG -> {
                        parser.nextToken(); // Move to the start of the object
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            fieldName = parser.currentName();
                            if (PAGES_TAG.equals(fieldName)) {
                                // Move to the start of the array and stop
                                parser.nextToken();
                                assert parser.getCurrentToken() == JsonToken.START_ARRAY;
                                return;
                            } else {
                                parser.skipChildren();
                            }
                        }
                    }
                    default -> parser.skipChildren();
                }
            }
        }

        @SneakyThrows
        @Override
        public boolean hasNext() {
            return parser.nextToken() != JsonToken.END_ARRAY && parser.getCurrentToken() == JsonToken.START_OBJECT;
        }

        @SneakyThrows
        @Override
        public WikipediaPage next() {
            return parsePage();
        }

        private WikipediaPage parsePage() throws Exception {
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                switch (fieldName) {
                    case ID_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentId = parser.getValueAsInt();
                    }
                    case NAMESPACE_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentNamespace = parser.getValueAsInt();
                    }
                    case MISSING_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentMissing = parser.getValueAsBoolean();
                    }
                    case TITLE_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentTitle = parser.getValueAsString();
                    }
                    case REDIRECT_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentRedirect = parser.getValueAsBoolean();
                    }
                    case PROTECTION_TAG -> parseProtections();
                    case REVISIONS_TAG -> parseRevisions();
                    default -> parser.skipChildren();
                }
            }

            WikipediaPage currentPage = buildCurrentPage();
            this.resetCurrentValues();
            return currentPage;
        }

        private WikipediaPage buildCurrentPage() {
            // If the page is missing, it is possible the content is not available.
            if (currentMissing) {
                currentContent = "NO CONTENT";
                currentTimestamp = WikipediaTimestamp.now().toString();
            }

            return WikipediaPage.builder()
                .pageKey(PageKey.of(lang, currentId))
                .namespace(WikipediaNamespace.valueOf(currentNamespace))
                .title(currentTitle)
                .content(currentContent)
                .lastUpdate(WikipediaTimestamp.of(currentTimestamp))
                .queryTimestamp(WikipediaTimestamp.of(currentCurTimestamp))
                .missing(currentMissing)
                .redirect(currentRedirect)
                .isProtected(currentProtected)
                .build();
        }

        private void resetCurrentValues() {
            this.currentTimestamp = null;
            this.currentContent = null;
            this.currentMissing = false;
            this.currentRedirect = false;
            this.currentProtected = false;
        }

        private void parseProtections() throws Exception {
            // Move into the array
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                // Iterate over each item in the array
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        parseProtection();
                    }
                }
            }
        }

        private void parseProtection() throws Exception {
            String type = null;
            String level = null;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                switch (fieldName) {
                    case TYPE_TAG -> {
                        parser.nextToken(); // Move to the value
                        type = parser.getValueAsString();
                    }
                    case LEVEL_TAG -> {
                        parser.nextToken(); // Move to the value
                        level = parser.getValueAsString();
                    }
                    default -> parser.skipChildren();
                }
            }

            if ("edit".equals(type) && "sysop".equals(level)) {
                this.currentProtected = true;
            }
        }

        private void parseRevisions() throws Exception {
            // Move into the array
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                // Iterate over each item in the array
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                        parseRevision();
                    }
                }
            }
        }

        private void parseRevision() throws Exception {
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                switch (fieldName) {
                    case TIMESTAMP_TAG -> {
                        parser.nextToken(); // Move to the value
                        this.currentTimestamp = parser.getValueAsString();
                    }
                    case SLOTS_TAG -> parseSlots();
                    default -> parser.skipChildren();
                }
            }
        }

        private void parseSlots() throws Exception {
            parser.nextToken(); // Move to the start of the object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                if (MAIN_TAG.equals(fieldName)) {
                    parseMain();
                } else {
                    parser.skipChildren();
                }
            }
        }

        private void parseMain() throws Exception {
            parser.nextToken(); // Move to the start of the object
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                if (CONTENT_TAG.equals(fieldName)) {
                    parser.nextToken(); // Move to the value
                    this.currentContent = parser.getValueAsString();
                } else {
                    parser.skipChildren();
                }
            }
        }
    }
}
