package es.bvalero.replacer.dump;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor // Needed by JPA
@Entity
@Table(name = "indexation")
class IndexationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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

    @Column(nullable = false)
    private Long end;

}
