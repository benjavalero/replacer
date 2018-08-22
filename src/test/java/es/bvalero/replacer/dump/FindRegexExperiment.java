package es.bvalero.replacer.dump;

import dk.brics.automaton.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindRegexExperiment {

    public static void main(String[] args) {
        String dumpFile = "/Users/benja/Developer/pywikibot/20180801/eswiki-20180801-pages-meta-current.xml.bz2";

        String regex = "[|=:][^}|=:\n]+\\.(gif|jpe?g|JPG|mp3|mpg|ogg|ogv|pdf|PDF|png|PNG|svg|tif|webm)";
        System.out.println("REGEX: " + regex);

        System.out.println("Start parsing dump file: " + dumpFile);

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();
            InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile));

            // Regex-directed
            final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            // Text-directed
            final RegExp r = new RegExp(regex);
            final Automaton a = r.toAutomaton(new DatatypesAutomatonProvider());
            final RunAutomaton ra = new RunAutomaton(a);

            DumpHandler dumpHandler = new DumpHandler() {
                @Override
                void processArticle(DumpArticle article) {
                    String text = this.getCurrentArticle().getContent();

                    AutomatonMatcher matcher = ra.newMatcher(text);
                    //Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        System.out.println("MATCH: " + matcher.group(0));
                    }
                }
            };

            saxParser.parse(xmlInput, dumpHandler);

            xmlInput.close();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        System.out.println("Finished parsing dump file");
    }

}
