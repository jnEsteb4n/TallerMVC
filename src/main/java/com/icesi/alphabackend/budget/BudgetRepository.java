package com.icesi.alphabackend.budget;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);
}
