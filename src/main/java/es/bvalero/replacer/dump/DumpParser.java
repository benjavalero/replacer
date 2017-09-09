package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.Date;

/**
 * Takes the most recent dump and parses it.
 * For each found article it performs different actions,
 * like finding the misspellings and storing them in the database.
 * To be run periodically or manually.
 */
@Component
class DumpParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpParser.class);
    // private static final int NUM_ARTICLES = 3002445 + 60257;

    private Date dtStart;
    private Date dtEnd;

    @Autowired
    private DumpHandler dumpHandler;

    @Autowired
    private DumpFinder dumpFinder;

    Date getDtStart() {
        return dtStart;
    }

    void setDtStart(Date dtStart) {
        this.dtStart = dtStart;
    }

    Date getDtEnd() {
        return dtEnd;
    }

    void setDtEnd(Date dtEnd) {
        this.dtEnd = dtEnd;
    }

    // To run every day but not immediately after deployment (1h delay)
    @Scheduled(initialDelay = 3600000, fixedDelay = 86400000)
    void run() {
        // TODO To be called manually from outside

        // Start the task
        if (this.isRunning()) {
            return;
        } else {
            this.setDtStart(new Date());
        }

        try {
            // Find the latest dump and check if we should parse again
            // In case of re-deployment of the application we will always parse
            Date latestDumpDate = dumpFinder.findLatestDumpDate();
            if (this.getDtEnd() == null || this.getDtEnd().before(latestDumpDate)) {
                File latestDumpFile = dumpFinder.findLatestDumpFile();
                parseDumpFile(latestDumpFile);
            }
            // TODO Print the number of items processed
        } catch (FileNotFoundException e) {
            LOGGER.error("Latest dump file not found", e);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error parsing dump file", e);
        } finally {
            // Finish the task
            this.setDtEnd(new Date());
        }
    }

    boolean isRunning() {
        return this.getDtStart() != null
                && (this.getDtEnd() == null || this.getDtEnd().before(this.getDtStart()));
    }

    // TODO Implement getMessage to report the parsing status
/*
    public String getMessage() {
        int numProcessed = articlesHandler.getNumItemsProcessed();
        if (isRunning()) {
            double percentProgress = numProcessed / NUM_ARTICLES * 100;
            double averageTimePerItem = (new Date().getTime() - this.getDtStart().getTime()) / numProcessed;
            long estimatedFinishTime = this.getDtStart().getTime() + (long) (averageTimePerItem * NUM_ARTICLES);
            return "Indexing...(" + percentProgress + " %). Estimated completion at " + new Date(estimatedFinishTime);
        } else {
            return "Last indexation completed on " + this.getDtEnd() + ". "
                    + "Articles processed: " + numProcessed;
        }
    }
*/
    private void parseDumpFile(File dumpFile)
            throws ParserConfigurationException, SAXException, IOException {
        // TODO To implement the progress, the total, etc.
        LOGGER.info("Parsing dump file: {}...", dumpFile);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();
        InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile));

        saxParser.parse(xmlInput, dumpHandler);
        xmlInput.close();

        LOGGER.info("Finished parsing dump file: {}...", dumpFile);
    }

}
