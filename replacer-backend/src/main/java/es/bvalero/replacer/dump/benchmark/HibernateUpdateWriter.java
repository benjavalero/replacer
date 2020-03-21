package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementRepository;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HibernateUpdateWriter extends HibernateItemWriter<ReplacementEntity> {

    private ReplacementRepository replacementRepository;

    public HibernateUpdateWriter(SessionFactory sessionFactory, ReplacementRepository repository) {
        super();
        setSessionFactory(sessionFactory);
        this.replacementRepository = repository;
    }

    @Override
    @Transactional
    public void write(@NotNull List<? extends ReplacementEntity> replacements) {
        int minId = replacements.stream().mapToInt(ReplacementEntity::getArticleId).min().orElse(0);
        int maxId = replacements.stream().mapToInt(ReplacementEntity::getArticleId).max().orElse(Integer.MAX_VALUE);
        List<ReplacementEntity> dbReps = replacementRepository.findByArticles(minId, maxId);
        List<ReplacementEntity> toUpdate = new ArrayList<>();
        for (int i = 0; i < dbReps.size(); i++) {
            if (i % 5 == 0) {
                dbReps.get(i).setLastUpdate(LocalDate.now().plusDays(1));
                toUpdate.add(dbReps.get(i));
            }
        }
        super.write(toUpdate);
    }
}
