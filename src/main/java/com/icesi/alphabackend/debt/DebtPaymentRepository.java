package com.icesi.alphabackend.debt;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {
    List<DebtPayment> findByDebtId(Long debtId);
}
