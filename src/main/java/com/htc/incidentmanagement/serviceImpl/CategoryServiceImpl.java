package com.htc.incidentmanagement.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.incidentmanagement.dto.CategoryRequest;
import com.htc.incidentmanagement.dto.CategoryResponse;
import com.htc.incidentmanagement.dto.TicketSummary;
import com.htc.incidentmanagement.exception.DuplicateResourceException;
import com.htc.incidentmanagement.exception.ResourceNotFoundException;
import com.htc.incidentmanagement.model.Category;
import com.htc.incidentmanagement.repository.CategoryRepository;
import com.htc.incidentmanagement.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        logger.info("CategoryService initialized.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getTickets()
                                .stream()
                                .map(ticket -> new TicketSummary(
                                        ticket.getTicketId(),
                                        ticket.getTitle(),
                                        ticket.getPriority(),
                                        ticket.getStatus()))
                                .toList()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getTickets()
                        .stream()
                        .map(ticket -> new TicketSummary(
                                ticket.getTicketId(),
                                ticket.getTitle(),
                                ticket.getPriority(),
                                ticket.getStatus()))
                        .toList());
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new DuplicateResourceException("Category", "name: " + request.getCategoryName());
        }
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        Category saved = categoryRepository.save(category);
        logger.info("Category created: id={}, name={}", saved.getCategoryId(), saved.getCategoryName());
        return new CategoryResponse(saved.getCategoryId(), saved.getCategoryName(), List.of());
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (!existing.getCategoryName().equalsIgnoreCase(request.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new DuplicateResourceException("Category", "name: " + request.getCategoryName());
        }
        existing.setCategoryName(request.getCategoryName());
        Category saved = categoryRepository.save(existing);
        logger.info("Category updated: id={}, name={}", saved.getCategoryId(), saved.getCategoryName());
        return new CategoryResponse(saved.getCategoryId(), saved.getCategoryName(), List.of());
    }

    @Override
    public void deleteCategory(Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        categoryRepository.delete(existing);
        logger.info("Category deleted: id={}", id);
    }
}
