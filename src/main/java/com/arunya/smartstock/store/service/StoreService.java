package com.arunya.smartstock.store.service;


import com.arunya.smartstock.store.dto.*;
import com.arunya.smartstock.store.entity.Store;
import com.arunya.smartstock.store.respository.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StoreService {

    private final StoreRepository repository;

    public StoreService(StoreRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StoreResponse create(StoreRequest request) {
        if (repository.existsByStoreCode(request.storeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Store code already exists");
        }

        Store store = new Store(
                request.storeCode(),
                request.name(),
                request.city(),
                request.province(),
                request.address()
        );

        return StoreResponse.from(repository.saveAndFlush(store));
    }

    @Transactional(readOnly = true)
    public List<StoreResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(StoreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StoreResponse findById(Long id) {
        return StoreResponse.from(getStore(id));
    }

    @Transactional
    public StoreResponse update(Long id, StoreRequest request) {
        Store store = getStore(id);

        if (!store.getStoreCode().equals(request.storeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Store code cannot be changed");
        }

        store.update(
                request.name(),
                request.city(),
                request.province(),
                request.address()
        );

        return StoreResponse.from(repository.saveAndFlush(store));
    }

    @Transactional
    public StoreResponse updateStatus(Long id, StoreStatusRequest request) {
        Store store = getStore(id);
        store.setActive(request.active());

        return StoreResponse.from(repository.saveAndFlush(store));
    }

    private Store getStore(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Store not found: " + id));
    }
}