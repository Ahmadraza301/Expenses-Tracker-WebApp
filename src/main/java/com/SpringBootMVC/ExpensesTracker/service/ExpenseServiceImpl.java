package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    ExpenseRepository expenseRepository;
    ClientService clientService;
    CategoryService categoryService;
    EntityManager entityManager;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ClientService clientService
            , CategoryService categoryService, EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.clientService = clientService;
        this.categoryService = categoryService;
        this.entityManager = entityManager;
    }


    @Override
    public Expense findExpenseById(int id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(ExpenseDTO expenseDTO) {
        System.out.println(expenseDTO);
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDateTime(expenseDTO.getDateTime());
        expense.setDescription(expenseDTO.getDescription());
        expense.setClient(clientService.findClientById(expenseDTO.getClientId()));
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        expense.setCategory(category);
        expenseRepository.save(expense);
    }

    @Override
    public void update(ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseDTO.getExpenseId()).orElse(null);
        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setDateTime(expenseDTO.getDateTime());
        existingExpense.setDescription(expenseDTO.getDescription());
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        existingExpense.setCategory(category);
        expenseRepository.save(existingExpense);
    }

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public List<Expense> findAllExpensesByClientId(int id) {
        return expenseRepository.findByClientId(id);
    }

    @Override
    public void deleteExpenseById(int id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> findFilterResult(FilterDTO filter) {
        String query = "select e from Expense e where e.client.id = " + filter.getClientId();
        
        if (!"all".equals(filter.getCategory())) {
            String category = filter.getCategory();
            int categoryId = categoryService.findCategoryByName(category).getId();
            query += String.format(" AND e.category.id = %d", categoryId);
        }
        
        int from = filter.getFrom();
        int to = filter.getTo();
        // Only add amount filter if values are provided (not both 0)
        if (from > 0 || to > 0) {
            // If to is 0, set it to a very large number
            if (to == 0) {
                to = Integer.MAX_VALUE;
            }
            query += String.format(" AND e.amount between %d and %d", from, to);
        }
        
        if (!"all".equals(filter.getYear())) {
            query += String.format(" AND CAST(SUBSTRING(e.dateTime, 1, 4) AS INTEGER) = %s", filter.getYear());
        }
        if (!"all".equals(filter.getMonth())) {
            query += String.format(" AND CAST(SUBSTRING(e.dateTime, 6, 2) AS INTEGER) = %s", filter.getMonth());
        }
        
        TypedQuery<Expense> expenseTypedQuery = entityManager.createQuery(query, Expense.class);
        List<Expense> expenseList = expenseTypedQuery.getResultList();
        return expenseList;
    }




}
