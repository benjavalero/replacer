package es.bvalero.replacer.dump;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor // Needed by JPA
@Entity
@Table(name = "indexation")
class DumpIndexation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean forced;

    @Column(nullable = false)
    private LocalDate start;

    DumpIndexation(String filename, boolean forced, LocalDate start) {
        this.filename = filename;
        this.forced = forced;
        this.start = start;
    }
}
