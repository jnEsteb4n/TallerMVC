package com.icesi.alphabackend.recurring;

import com.icesi.alphabackend.account.Account;
import com.icesi.alphabackend.category.Category;
import com.icesi.alphabackend.movement.Movement;
import com.icesi.alphabackend.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Periodicity periodicity;

    @Column(name = "next_occurrence_date", nullable = false)
    private LocalDate nextOccurrenceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurringStatus status = RecurringStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recurringTransaction")
    private List<Movement> movements = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Periodicity getPeriodicity() { return periodicity; }
    public void setPeriodicity(Periodicity periodicity) { this.periodicity = periodicity; }
    public LocalDate getNextOccurrenceDate() { return nextOccurrenceDate; }
    public void setNextOccurrenceDate(LocalDate nextOccurrenceDate) { this.nextOccurrenceDate = nextOccurrenceDate; }
    public RecurringStatus getStatus() { return status; }
    public void setStatus(RecurringStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Movement> getMovements() { return movements; }
    public void setMovements(List<Movement> movements) { this.movements = movements; }
}
