package com.bank.bankingsimulator.repository;

import com.bank.bankingsimulator.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
