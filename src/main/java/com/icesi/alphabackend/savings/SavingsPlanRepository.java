package com.icesi.alphabackend.savings;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsPlanRepository extends JpaRepository<SavingsPlan, Long> {
    List<SavingsPlan> findByOwnerId(Long ownerId);
}
