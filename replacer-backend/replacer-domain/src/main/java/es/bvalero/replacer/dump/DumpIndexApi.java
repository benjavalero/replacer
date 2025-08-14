package es.bvalero.replacer.dump;

import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface DumpIndexApi {
    /** Find the status of the current (or the last) dump indexing */
    Optional<DumpStatus> getDumpStatus();

    /** Find the latest dump files for each language and index them */
    void indexLatestDumpFiles();
}
