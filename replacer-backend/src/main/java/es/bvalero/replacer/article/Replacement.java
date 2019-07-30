package es.bvalero.replacer.article;

import lombok.*;
import lombok.experimental.Wither;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * A replacement in the database related to an article.
 */
@Data
@NoArgsConstructor // Needed by JPA
@AllArgsConstructor // Needed by @Wither
@Entity
@Table(name = "replacement2",
        uniqueConstraints = @UniqueConstraint(columnNames = {"articleId", "type", "subtype", "position"}))
public class Replacement implements Serializable {

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
    @Wither
    private LocalDate lastUpdate;

    @Column
    @Wither
    private String reviewer;

    public Replacement(int articleId, String type, String subtype, int position) {
        this.articleId = articleId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
        this.reviewer = null;
    }

    boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    boolean isSame(Replacement that) {
        return articleId == that.articleId &&
                position == that.position &&
                type.equals(that.type) &&
                subtype.equals(that.subtype);
    }

}

