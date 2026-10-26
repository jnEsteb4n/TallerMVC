package com.icesi.alphabackend.savings;

import com.icesi.alphabackend.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "savings_plan_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "user_id"}))
public class SavingsPlanParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SavingsPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "individual_goal_amount", precision = 19, scale = 2)
    private BigDecimal individualGoalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantStatus status = ParticipantStatus.INVITED;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingsContribution> contributions = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (invitedAt == null) {
            invitedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SavingsPlan getPlan() { return plan; }
    public void setPlan(SavingsPlan plan) { this.plan = plan; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getIndividualGoalAmount() { return individualGoalAmount; }
    public void setIndividualGoalAmount(BigDecimal individualGoalAmount) { this.individualGoalAmount = individualGoalAmount; }
    public ParticipantStatus getStatus() { return status; }
    public void setStatus(ParticipantStatus status) { this.status = status; }
    public LocalDateTime getInvitedAt() { return invitedAt; }
    public void setInvitedAt(LocalDateTime invitedAt) { this.invitedAt = invitedAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public List<SavingsContribution> getContributions() { return contributions; }
    public void setContributions(List<SavingsContribution> contributions) { this.contributions = contributions; }
}
