package es.bvalero.replacer.dump;

import java.io.File;
import java.util.Date;

/**
 * Domain class corresponding to a Wikipedia XML dump.
 */
class DumpFile {

    private File file;
    private Date date;

    File getFile() {
        return file;
    }

    void setFile(File file) {
        this.file = file;
    }

    Date getDate() {
        return date;
    }

    void setDate(Date date) {
        this.date = date;
    }

}
