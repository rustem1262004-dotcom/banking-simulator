package com.bank.bankingsimulator.repository;

import com.bank.bankingsimulator.entity.BudgetLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetLimitRepository extends JpaRepository<BudgetLimit, Long> {
    List<BudgetLimit> findByUserIdAndMonth(Long userId, String month);
}