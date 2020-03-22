package es.bvalero.replacer.replacement;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A replacement in the database related to an article.
 */
@Data
@NoArgsConstructor // Needed by JPA
@Entity
@Table(name = "repl",
        uniqueConstraints = @UniqueConstraint(columnNames = {"articleId", "type", "subtype", "position"}))
public class ReplacementEntity implements Serializable {

    private static final long serialVersionUID = -6766305982117992712L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private int articleId;

    @Column(nullable = false, length = 25)
    private String type;

    @Column(nullable = false, length = 30)
    private String subtype;

    @Column
    @ColumnDefault(value = "0")
    private int position;

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
