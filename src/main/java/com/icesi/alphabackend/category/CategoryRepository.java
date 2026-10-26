package com.icesi.alphabackend.category;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByOwnerIsNull();

    List<Category> findByOwnerId(Long userId);
}
