package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;

import javax.persistence.EntityManagerFactory;

public class HibernateInsertWriter extends HibernateItemWriter<ReplacementEntity> {

    public HibernateInsertWriter(SessionFactory sessionFactory) {
        super();
        setSessionFactory(sessionFactory);
    }
}
