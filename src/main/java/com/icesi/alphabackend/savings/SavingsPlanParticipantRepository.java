package com.icesi.alphabackend.savings;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsPlanParticipantRepository extends JpaRepository<SavingsPlanParticipant, Long> {
    List<SavingsPlanParticipant> findByPlanId(Long planId);

    List<SavingsPlanParticipant> findByUserId(Long userId);
}
