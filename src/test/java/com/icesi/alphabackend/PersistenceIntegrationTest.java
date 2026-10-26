package com.icesi.alphabackend;

import com.icesi.alphabackend.account.AccountRepository;
import com.icesi.alphabackend.budget.BudgetRepository;
import com.icesi.alphabackend.category.CategoryRepository;
import com.icesi.alphabackend.common.BusinessException;
import com.icesi.alphabackend.debt.Debt;
import com.icesi.alphabackend.debt.DebtPaymentRepository;
import com.icesi.alphabackend.debt.DebtRepository;
import com.icesi.alphabackend.movement.MovementRepository;
import com.icesi.alphabackend.recurring.RecurringStatus;
import com.icesi.alphabackend.recurring.RecurringTransactionRepository;
import com.icesi.alphabackend.role.Role;
import com.icesi.alphabackend.role.RoleService;
import com.icesi.alphabackend.savings.SavingsContributionRepository;
import com.icesi.alphabackend.savings.SavingsPlan;
import com.icesi.alphabackend.savings.SavingsPlanParticipantRepository;
import com.icesi.alphabackend.savings.SavingsPlanRepository;
import com.icesi.alphabackend.user.User;
import com.icesi.alphabackend.user.UserRepository;
import com.icesi.alphabackend.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenceIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MovementRepository movementRepository;
    @Autowired RecurringTransactionRepository recurringRepository;
    @Autowired BudgetRepository budgetRepository;
    @Autowired DebtRepository debtRepository;
    @Autowired DebtPaymentRepository debtPaymentRepository;
    @Autowired SavingsPlanRepository savingsPlanRepository;
    @Autowired SavingsPlanParticipantRepository participantRepository;
    @Autowired SavingsContributionRepository contributionRepository;
    @Autowired UserService userService;
    @Autowired RoleService roleService;

    @Test
    void seedDataIsLoadedAndRelationsAreMapped() {
        User ana = userRepository.findByEmail("ana.torres@u.icesi.edu.co").orElseThrow();

        assertEquals(1, ana.getRoles().size());
        Role role = ana.getRoles().iterator().next();
        assertEquals("USER", role.getName());
        assertFalse(role.getPermissions().isEmpty());
        assertEquals(2, accountRepository.findByUserId(ana.getId()).size());
        assertEquals(3, movementRepository.findByUserIdOrderByMovementDateDesc(ana.getId()).size());
        assertEquals(6, categoryRepository.findByOwnerIsNull().size());
        assertEquals(1, categoryRepository.findByOwnerId(ana.getId()).size());
        assertEquals(1, recurringRepository.findByStatus(RecurringStatus.ACTIVE).size());
        assertEquals(2, budgetRepository.findByUserIdAndYearAndMonth(ana.getId(), 2026, 9).size());
    }

    @Test
    void debtAggregatesItsPayments() {
        Debt debt = debtRepository.findAll().get(0);

        assertEquals(1, debt.getPayments().size());
        assertEquals(1, debtPaymentRepository.findByDebtId(debt.getId()).size());
        assertEquals(new BigDecimal("20000.00"), debt.getPayments().get(0).getAmount().setScale(2));
    }

    @Test
    void sharedSavingsPlanHasParticipantsAndContributions() {
        SavingsPlan plan = savingsPlanRepository.findAll().get(0);

        assertTrue(plan.isShared());
        assertEquals(2, participantRepository.findByPlanId(plan.getId()).size());
        assertEquals(2, contributionRepository.findAll().size());
        assertEquals(1, contributionRepository.findByParticipantId(plan.getParticipants().get(0).getId()).size());
    }

    @Test
    void userServiceCreatesUserWithRoleAndRefusesToRemoveItsLastRole() {
        User created = userService.create("Nuevo Usuario", "nuevo@u.icesi.edu.co", "Password123", "Derecho", Set.of(2L));

        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertNotEquals("Password123", created.getPasswordHash());
        assertThrows(BusinessException.class, () -> userService.removeRole(created.getId(), 2L));
        assertThrows(BusinessException.class,
                () -> userService.create("Otro", "otro@u.icesi.edu.co", "Password123", null, Set.of()));
    }

    @Test
    void roleServiceCreatesRoleWithPermissionsAndRefusesToRemoveTheLastOne() {
        Role role = roleService.create("MODERADOR", "Modera contenido", Set.of(1L));

        assertEquals(1, role.getPermissions().size());
        assertThrows(BusinessException.class, () -> roleService.removePermission(role.getId(), 1L));
        assertThrows(BusinessException.class, () -> roleService.delete(2L));
    }
}
