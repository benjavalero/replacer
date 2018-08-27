package es.bvalero.replacer.dump;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArticleSizeExperiment {

    public static void main(String[] args) {
        // We use de unzipped version because in my laptop it takes about 15 times less (about 10')
        String dumpFile = "/Users/benja/Developer/pywikibot/20180801/eswiki-20180801-pages-meta-current.xml";
        System.out.println("Start parsing dump file: " + dumpFile);

        try (InputStream xmlInput = new FileInputStream(dumpFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
            SAXParser saxParser = factory.newSAXParser();

            final List<Integer> sizes = new ArrayList<>(1500000);
            // We use arrays as a trick to define final variables
            final int[] maxSizes = {0};
            final String[] maxArticles = {""};
            final long startTime = new Date().getTime();
            DumpHandler dumpHandler = new DumpHandler() {
                @Override
                void processArticle(DumpArticle article) {
                    int currentSize = this.getCurrentArticle().getContent().length() / 1024;
                    sizes.add(currentSize);
                    if (currentSize > maxSizes[0]) {
                        maxSizes[0] = currentSize;
                        maxArticles[0] = this.getCurrentArticle().getTitle();
                    }
                    if (this.getDumpStatus().getPagesCount() % 1000 == 0) {
                        long elapsedTime = (new Date().getTime() - startTime) / 1000;
                        System.out.println(this.getDumpStatus().getPagesCount() + "\t" + elapsedTime + " s");
                    }
                }
            };

            saxParser.parse(xmlInput, dumpHandler);

            final String sizesStr = StringUtils.join(sizes, "\n");
            Files.write(Paths.get("./sizes.txt"), sizesStr.getBytes());

            System.out.println("Longest article: " + maxArticles[0]);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        System.out.println("Finished parsing dump file");
    }

}
