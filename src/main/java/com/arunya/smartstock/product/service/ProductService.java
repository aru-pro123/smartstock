package com.arunya.smartstock.product.service;

import com.arunya.smartstock.category.entity.Category;
import com.arunya.smartstock.category.repository.CategoryRepository;
import com.arunya.smartstock.product.dto.*;
import com.arunya.smartstock.product.entity.Product;
import com.arunya.smartstock.product.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "SKU already exists"
            );
        }

        if (request.barcode() != null &&
                productRepository.existsByBarcode(request.barcode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Barcode already exists"
            );
        }

        Category category = getCategory(request.categoryId());

        Product product = new Product(
                request.sku(),
                request.barcode(),
                request.name(),
                request.brand(),
                category,
                request.unitOfMeasure(),
                request.sellingPrice(),
                request.taxable(),
                request.perishable(),
                request.shelfLifeDays()
        );

        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getProduct(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {

        Product product = getProduct(id);

        if (!product.getSku().equals(request.sku())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SKU cannot be changed"
            );
        }

        if (request.barcode() != null &&
                productRepository.existsByBarcode(request.barcode()) &&
                !request.barcode().equals(product.getBarcode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Barcode already exists"
            );
        }

        Category category = getCategory(request.categoryId());

        product.update(
                request.barcode(),
                request.name(),
                request.brand(),
                category,
                request.unitOfMeasure(),
                request.sellingPrice(),
                request.taxable(),
                request.perishable(),
                request.shelfLifeDays()
        );

        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse updateStatus(
            Long id,
            ProductStatusRequest request
    ) {
        Product product = getProduct(id);

        product.setActive(request.active());

        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found: " + id
                ));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + id
                ));
    }
}