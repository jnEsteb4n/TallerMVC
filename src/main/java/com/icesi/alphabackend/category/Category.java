package com.icesi.alphabackend.category;

import com.icesi.alphabackend.common.TransactionType;
import com.icesi.alphabackend.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    // Nulo = categoría predefinida por el administrador.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
