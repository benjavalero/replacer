package es.bvalero.replacer.dump;

import dk.brics.automaton.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class FindRegexExperiment {

    public static void main(String[] args) {
        String dumpFile = "/Users/benja/Developer/pywikibot/20180820/eswiki-20180820-pages-articles.xml";

        String regex = "(<P>|<Z>) Enero";
        System.out.println("REGEX: " + regex);

        System.out.println("Start parsing dump file: " + dumpFile);

        try (InputStream xmlInput = new FileInputStream(dumpFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            // Regex-directed
            final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            // Text-directed
            final RegExp r = new RegExp(regex);
            final Automaton a = r.toAutomaton(new DatatypesAutomatonProvider());
            final RunAutomaton ra = new RunAutomaton(a);

            DumpHandler dumpHandler = new DumpHandler(new DumpProcessor()) {
                @Override
                boolean processArticle(DumpArticle dumpArticle) {
                    String text = dumpArticle.getContent();

                    AutomatonMatcher matcher = ra.newMatcher(text);
                    //Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        System.out.println("MATCH: " + matcher.group(0));
                    }

                    return true;
                }
            };

            saxParser.parse(xmlInput, dumpHandler);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        System.out.println("Finished parsing dump file");
    }

}
