package com.icesi.alphabackend.debt;

import com.icesi.alphabackend.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "debts")
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "counterparty_name", nullable = false, length = 150)
    private String counterpartyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DebtDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "origin_date", nullable = false)
    private LocalDate originDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DebtStatus status = DebtStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "debt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DebtPayment> payments = new ArrayList<>();

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
    public String getCounterpartyName() { return counterpartyName; }
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }
    public DebtDirection getDirection() { return direction; }
    public void setDirection(DebtDirection direction) { this.direction = direction; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getOriginDate() { return originDate; }
    public void setOriginDate(LocalDate originDate) { this.originDate = originDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public DebtStatus getStatus() { return status; }
    public void setStatus(DebtStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<DebtPayment> getPayments() { return payments; }
    public void setPayments(List<DebtPayment> payments) { this.payments = payments; }
}
