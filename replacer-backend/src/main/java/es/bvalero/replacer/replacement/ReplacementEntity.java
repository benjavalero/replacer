package es.bvalero.replacer.replacement;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.jetbrains.annotations.TestOnly;

/**
 * A replacement in the database related to an article.
 */
@Data
@NoArgsConstructor // Needed by JPA
@AllArgsConstructor
@Entity
@Table(name = "replacement2")
public class ReplacementEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int articleId;

    @Column(nullable = false, length = 25)
    private String type;

    @Column(nullable = false, length = 30)
    private String subtype;

    @Column
    @ColumnDefault(value = "0")
    private int position;

    @Column
    private String context;

    @Column(nullable = false)
    private LocalDate lastUpdate;

    @Column
    private String reviewer;

    public ReplacementEntity(int articleId, String type, String subtype, int position) {
        this.articleId = articleId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
        this.reviewer = null;
    }

    @TestOnly
    ReplacementEntity(int articleId, String type, String subtype, int position, String reviewer) {
        this.articleId = articleId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
        this.reviewer = reviewer;
    }

    boolean isToBeReviewed() {
        return this.reviewer == null;
    }
}
