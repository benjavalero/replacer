package es.bvalero.replacer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

abstract class AbstractDao<K extends Serializable, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDao.class);
    private final Class<T> persistentClass;

    @PersistenceContext
    private EntityManager entityManager;

    // TODO Extract DB properties

    AbstractDao() {
        this.persistentClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[1];
    }

    EntityManager getEntityManager() {
        return entityManager;
    }

    T findById(K key) {
        return entityManager.find(persistentClass, key);
    }

    List<T> findAll() {
        Query query = entityManager.createQuery("FROM " + persistentClass.getName());
        return query.getResultList();
    }

    private void insert(T entity) {
        entityManager.persist(entity);
    }

    void insertAll(List<T> entities) {
        for (T entity : entities) {
            try {
                insert(entity);
            } catch (Exception e) {
                LOGGER.warn("Error inserting entity: {}", entity, e);
            }
        }
    }

    private void update(T entity) {
        entityManager.merge(entity);
    }

    void updateAll(List<T> entities) {
        for (T entity : entities) {
            try {
                update(entity);
            } catch (Exception e) {
                LOGGER.warn("Error updating entity: {}", entity, e);
            }
        }
    }

    private void delete(T entity) {
        entityManager.remove(entity);
    }

    void deleteAll(List<T> entities) {
        for (T entity : entities) {
            try {
                delete(entity);
            } catch (Exception e) {
                LOGGER.warn("Error deleting entity: {}", entity, e);
            }
        }
    }

}