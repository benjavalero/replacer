package es.bvalero.replacer.dump;

import dk.brics.automaton.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public final class FindRegexExperiment {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindRegexExperiment.class);

    private FindRegexExperiment() {
    }

    public static void main(String[] args) {
        String dumpFile = "/Users/benja/Developer/pywikibot/20180901/eswiki-20180901-pages-articles.xml";

        String regex = "(<P>|<Z>) Enero";
        LOGGER.info("REGEX: {}", regex);

        LOGGER.info("Start parsing dump file: {}", dumpFile);

        try (InputStream xmlInput = new FileInputStream(dumpFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Regex-directed
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            // Text-directed
            RegExp r = new RegExp(regex);
            Automaton a = r.toAutomaton(new DatatypesAutomatonProvider());
            RunAutomaton ra = new RunAutomaton(a);
            DumpHandler dumpHandler = new FindRegexExperiment.MyDumpHandler(ra, pattern);

            saxParser.parse(xmlInput, dumpHandler);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOGGER.error("", e);
        }

        LOGGER.info("Finished parsing dump file");
    }

    private static class MyDumpHandler extends DumpHandler {
        private final Pattern pattern;
        private final RunAutomaton ra;

        MyDumpHandler(RunAutomaton ra, Pattern pattern) {
            this.ra = ra;
            this.pattern = pattern;
        }

        @Override
        boolean processArticle(DumpArticle dumpArticle) {
            String text = dumpArticle.getContent();

            AutomatonMatcher matcher = ra.newMatcher(text);
            //Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                LOGGER.info("MATCH: {}", matcher.group(0));
            }

            return true;
        }
    }
}
