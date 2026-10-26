package com.icesi.alphabackend.recurring;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByStatus(RecurringStatus status);
}
