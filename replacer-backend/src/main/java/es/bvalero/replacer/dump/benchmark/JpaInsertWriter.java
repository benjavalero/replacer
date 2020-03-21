package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import javax.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;

public class JpaInsertWriter extends JpaItemWriter<ReplacementEntity> {

    public JpaInsertWriter(EntityManagerFactory emf) {
        super();
        setEntityManagerFactory(emf);
    }
}
