package es.bvalero.replacer.dump;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for dump indexations in database.
 */
@Repository
@Transactional
interface IndexationRepository extends JpaRepository<IndexationEntity, Long> {

    List<IndexationEntity> findByOrderByIdDesc(Pageable pageable);

}