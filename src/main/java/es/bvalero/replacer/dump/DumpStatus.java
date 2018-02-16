package es.bvalero.replacer.dump;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to store the status and figures of the current dump indexation.
 */
public class DumpStatus {

    private static final double NUM_ARTICLES = 3557238; // Rough amount of articles to be checked
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private boolean running;
    private Date startDate;
    private Date endDate;

    private int numProcessedItems;

    public boolean isRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    Date getStartDate() {
        return startDate;
    }

    void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    Date getEndDate() {
        return endDate;
    }

    void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    int getNumProcessedItems() {
        return numProcessedItems;
    }

    void setNumProcessedItems(int numProcessedItems) {
        this.numProcessedItems = numProcessedItems;
    }

    /**
     * @return A message about the indexation status to be displayed in the web client.
     */
    public String getMessage() {
        // TODO Cover with unit tests. Take into account the case when it's not running but the dump is old. Build the message in the client.
        StringBuilder message = new StringBuilder();
        if (isRunning()) {
            double percentProgress = getNumProcessedItems() / NUM_ARTICLES * 100;
            double averageTimePerItem = (new Date().getTime() - getStartDate().getTime()) / (double) getNumProcessedItems();
            long estimatedFinishTime = getStartDate().getTime() + (long) (averageTimePerItem * NUM_ARTICLES);
            message.append("La indexación se está ejecutando.")
                    .append("<ul>")
                    .append("<li>")
                    .append("Inicio: ")
                    .append(dateFormat.format(getStartDate()))
                    .append("</li>")
                    .append("<li>")
                    .append("Núm. artículos procesados: ")
                    .append(getNumProcessedItems())
                    .append(" (")
                    .append(DECIMAL_FORMAT.format(percentProgress))
                    .append(" %)</li>")
                    .append("<li>")
                    .append("Finalización estimada: ")
                    .append(dateFormat.format(new Date(estimatedFinishTime)))
                    .append("</li>")
                    .append("</ul>");
        } else {
            message.append("La indexación no se está ejecutando.");
            if (getEndDate() != null) {
                message.append("<ul>")
                        .append("<li>")
                        .append("Última ejecución: ")
                        .append(dateFormat.format(getEndDate()))
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

}
