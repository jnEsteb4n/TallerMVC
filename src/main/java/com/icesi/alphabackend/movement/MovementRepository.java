package com.icesi.alphabackend.movement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByUserIdOrderByMovementDateDesc(Long userId);

    List<Movement> findByAccountId(Long accountId);
}
