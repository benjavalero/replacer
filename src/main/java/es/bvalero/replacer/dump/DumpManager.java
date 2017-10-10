package es.bvalero.replacer.dump;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Takes the most recent dump and parses it.
 * For each found article it performs different actions,
 * like finding the misspellings and storing them in the database.
 * To be run periodically or manually.
 */
@Component
class DumpManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpManager.class);
    private static final double NUM_ARTICLES = 3002445 + 60257; // Rough amount of articles to be checked
    private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Autowired
    private DumpHandler dumpHandler;

    @Autowired
    private DumpFinder dumpFinder;

    @Value("${replacer.dump.folder.path:}")
    private String dumpFolderPath;

    private boolean running = false;
    private Date startDate;
    private Date endDate;

    private String getDumpFolderPath() {
        return dumpFolderPath;
    }

    void setDumpFolderPath(String dumpFolderPath) {
        this.dumpFolderPath = dumpFolderPath;
    }

    private boolean isRunning() {
        return running;
    }

    private void setRunning(boolean running) {
        this.running = running;
    }

    Date getStartDate() {
        return startDate;
    }

    private void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    Date getEndDate() {
        return endDate;
    }

    void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    // Re-index weekly
    @Scheduled(fixedDelay = 7 * 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    @Async
    void runIndexation() {
        // Start the task
        if (isRunning()) {
            // This should not happen but we check it just in case
            return;
        } else {
            setRunning(true);
            setStartDate(new Date());
        }

        try {
            File dumpFolderFile = new File(getDumpFolderPath());

            // Find the latest dump and check if we should parse again
            // In case of re-deployment of the application we will always parse
            Date latestDumpDate = dumpFinder.findLatestDumpDate(dumpFolderFile);
            if (getEndDate() != null && !getEndDate().before(latestDumpDate)) {
                return;
            }

            File latestDumpFile = dumpFinder.findLatestDumpFile(dumpFolderFile);
            parseDumpFile(latestDumpFile);
            LOGGER.info("Total number of articles processed: " + getNumProcessedItems());
        } catch (FileNotFoundException e) {
            LOGGER.error("Latest dump file not found", e);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Error parsing dump file", e);
        } finally {
            setEndDate(new Date());
            setRunning(false);
        }
    }

    private String getStatusMessage() {
        StringBuilder message = new StringBuilder();
        if (isRunning()) {
            double percentProgress = getNumProcessedItems() / NUM_ARTICLES * 100;
            double averageTimePerItem = (new Date().getTime() - getStartDate().getTime()) / getNumProcessedItems();
            long estimatedFinishTime = getStartDate().getTime() + (long) (averageTimePerItem * NUM_ARTICLES);
            message.append("La indexación se está ejecutando.")
                    .append("<ul>")
                    .append("<li>")
                    .append("Inicio: ")
                    .append(DATE_FORMAT.format(getStartDate()))
                    .append("</li>")
                    .append("<li>")
                    .append("Núm. artículos procesados: ")
                    .append(getNumProcessedItems())
                    .append(" (")
                    .append(DECIMAL_FORMAT.format(percentProgress))
                    .append(" %)</li>")
                    .append("<li>")
                    .append("Finalización estimada: ")
                    .append(DATE_FORMAT.format(new Date(estimatedFinishTime)))
                    .append("</li>")
                    .append("</ul>");
        } else {
            message.append("La indexación no se está ejecutando.");
            if (this.endDate != null) {
                message.append("<ul>")
                        .append("<li>")
                        .append("Última ejecución: ")
                        .append(DATE_FORMAT.format(getEndDate()))
                        .append("</li>")
                        .append("<li>")
                        .append("Núm. artículos procesados: ")
                        .append(getNumProcessedItems())
                        .append("</li>")
                        .append("</ul>");
            }
        }

        return message.toString();
    }

    private void parseDumpFile(File dumpFile)
            throws ParserConfigurationException, SAXException, IOException {
        LOGGER.info("Parsing dump file: {}...", dumpFile);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();
        InputStream xmlInput = new BZip2CompressorInputStream(new FileInputStream(dumpFile));

        saxParser.parse(xmlInput, dumpHandler);
        xmlInput.close();

        LOGGER.info("Finished parsing dump file: {}...", dumpFile);
    }

    DumpStatus getDumpStatus() {
        DumpStatus status = new DumpStatus();
        status.setRunning(isRunning());
        status.setMessage(this.getStatusMessage());
        return status;
    }

    private int getNumProcessedItems() {
        return dumpHandler.getNumItemsProcessed();
    }

}
