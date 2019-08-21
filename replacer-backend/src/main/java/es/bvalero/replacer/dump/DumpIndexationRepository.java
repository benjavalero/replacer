package es.bvalero.replacer.dump;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for replacements in database.
 */
@Repository
@Transactional
interface DumpIndexationRepository extends JpaRepository<DumpIndexation, Long> {

    List<DumpIndexation> findByOrderByIdDesc(Pageable pageable);

}