package com.icesi.alphabackend.savings;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsContributionRepository extends JpaRepository<SavingsContribution, Long> {
    List<SavingsContribution> findByParticipantId(Long participantId);
}
