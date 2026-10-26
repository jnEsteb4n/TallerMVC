package com.icesi.alphabackend.movement;

import com.icesi.alphabackend.account.Account;
import com.icesi.alphabackend.category.Category;
import com.icesi.alphabackend.common.TransactionType;
import com.icesi.alphabackend.recurring.RecurringTransaction;
import com.icesi.alphabackend.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movements")
public class Movement {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_transaction_id")
    private RecurringTransaction recurringTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(length = 500)
    private String description;

    @Column(name = "receipt_image_url", length = 500)
    private String receiptImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

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
    public RecurringTransaction getRecurringTransaction() { return recurringTransaction; }
    public void setRecurringTransaction(RecurringTransaction recurringTransaction) { this.recurringTransaction = recurringTransaction; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getMovementDate() { return movementDate; }
    public void setMovementDate(LocalDate movementDate) { this.movementDate = movementDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReceiptImageUrl() { return receiptImageUrl; }
    public void setReceiptImageUrl(String receiptImageUrl) { this.receiptImageUrl = receiptImageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
