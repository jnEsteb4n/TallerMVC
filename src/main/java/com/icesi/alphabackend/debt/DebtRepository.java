package com.icesi.alphabackend.debt;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {
    List<Debt> findByUserId(Long userId);

    List<Debt> findByUserIdAndStatus(Long userId, DebtStatus status);
}
