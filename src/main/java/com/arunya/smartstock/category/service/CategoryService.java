package com.arunya.smartstock.category.service;

import com.arunya.smartstock.category.dto.*;
import com.arunya.smartstock.category.entity.Category;
import com.arunya.smartstock.category.repository.CategoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {

        if (repository.existsByCategoryCode(request.categoryCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category code already exists"
            );
        }

        Category category = new Category(
                request.categoryCode(),
                request.name(),
                request.description()
        );

        return CategoryResponse.from(repository.saveAndFlush(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getCategory(id));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {

        Category category = getCategory(id);

        if (!category.getCategoryCode().equals(request.categoryCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category code cannot be changed"
            );
        }

        category.update(
                request.name(),
                request.description()
        );

        return CategoryResponse.from(repository.saveAndFlush(category));
    }

    @Transactional
    public CategoryResponse updateStatus(
            Long id,
            CategoryStatusRequest request
    ) {

        Category category = getCategory(id);

        category.setActive(request.active());

        return CategoryResponse.from(repository.saveAndFlush(category));
    }

    private Category getCategory(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + id
                ));
    }
}