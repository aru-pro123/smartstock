package com.arunya.smartstock.department.service;

import com.arunya.smartstock.department.dto.*;
import com.arunya.smartstock.department.entity.Department;
import com.arunya.smartstock.department.respository.DepartmentRepository;
import com.arunya.smartstock.store.entity.Store;
import com.arunya.smartstock.store.respository.StoreRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StoreRepository storeRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            StoreRepository storeRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional
    public DepartmentResponse create(
            Long storeId,
            DepartmentRequest request
    ) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store not found: " + storeId
                ));

        if (!store.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add departments to an inactive store"
            );
        }

        if (departmentRepository.existsByStoreIdAndDepartmentCode(
                storeId,
                request.departmentCode()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department code already exists in this store"
            );
        }

        Department department = new Department(
                store,
                request.departmentCode(),
                request.name(),
                request.description()
        );

        return DepartmentResponse.from(
                departmentRepository.saveAndFlush(department)
        );
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findByStore(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Store not found: " + storeId
            );
        }

        return departmentRepository.findByStoreId(storeId)
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return DepartmentResponse.from(getDepartment(id));
    }

    @Transactional
    public DepartmentResponse update(
            Long id,
            DepartmentRequest request
    ) {

        Department department = getDepartment(id);

        if (!department.getDepartmentCode()
                .equals(request.departmentCode())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Department code cannot be changed"
            );
        }

        department.update(
                request.name(),
                request.description()
        );

        return DepartmentResponse.from(
                departmentRepository.saveAndFlush(department)
        );
    }

    @Transactional
    public DepartmentResponse updateStatus(
            Long id,
            DepartmentStatusRequest request
    ) {

        Department department = getDepartment(id);

        if (request.active() && !department.getStore().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot activate a department in an inactive store"
            );
        }

        department.setActive(request.active());

        return DepartmentResponse.from(
                departmentRepository.saveAndFlush(department)
        );
    }

    private Department getDepartment(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found: " + id
                ));
    }
}