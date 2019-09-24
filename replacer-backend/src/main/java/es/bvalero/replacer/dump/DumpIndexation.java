package es.bvalero.replacer.dump;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor // Needed by JPA
@Entity
@Table(name = "indexation")
class DumpIndexation implements Serializable {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Transient
    private boolean running = false;

    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean forceProcess;

    @Column(nullable = false)
    private long numArticlesRead;

    @Column(nullable = false)
    private long numArticlesProcessable;

    @Column(nullable = false)
    private long numArticlesProcessed;

    @Column(nullable = false)
    private String dumpFileName;

    @Column(nullable = false)
    private long start;

    @Column
    private Long end;

    DumpIndexation(String dumpFileName, boolean forceProcess) {
        this.forceProcess = forceProcess;
        this.dumpFileName = dumpFileName;
        this.start = Instant.now().toEpochMilli();

        // Default values
        this.running = true;
        this.numArticlesRead = 0L;
        this.numArticlesProcessable = 0L;
        this.numArticlesProcessed = 0L;
        this.end = null;
    }

    void finish() {
        this.running = false;
        this.end = Instant.now().toEpochMilli();
    }

    void incrementNumArticlesRead() {
        this.numArticlesRead++;
    }

    void incrementNumArticlesProcessable() {
        this.numArticlesProcessable++;
    }

    void incrementNumArticlesProcessed() {
        this.numArticlesProcessed++;
    }

}
