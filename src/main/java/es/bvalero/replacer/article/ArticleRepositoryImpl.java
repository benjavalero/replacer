package es.bvalero.replacer.article;

import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import org.springframework.transaction.annotation.Transactional;

public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void clear() {
        em.clear();
    }

}
