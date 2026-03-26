package com.htc.incidentmanagement.service;

import java.util.List;

import com.htc.incidentmanagement.dto.CategoryRequest;
import com.htc.incidentmanagement.dto.CategoryResponse;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
