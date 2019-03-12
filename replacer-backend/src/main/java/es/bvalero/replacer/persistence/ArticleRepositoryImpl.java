package es.bvalero.replacer.persistence;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void clear() {
        em.clear();
    }

}
