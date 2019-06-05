package es.bvalero.replacer.article;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

class ReplacementRepositoryImpl implements ReplacementRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void clear() {
        em.clear();
    }

}
