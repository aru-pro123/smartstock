package com.arunya.smartstock.supplier.service;


import com.arunya.smartstock.supplier.dto.*;
import com.arunya.smartstock.supplier.entity.Supplier;
import com.arunya.smartstock.supplier.repository.SupplierRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(
            SupplierRepository supplierRepository
    ) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public SupplierResponse createSupplier(
            SupplierCreateRequest request
    ) {

        String name = request.name().trim();

        if (supplierRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Supplier already exists"
            );
        }

        Supplier supplier = new Supplier();

        supplier.setName(name);
        supplier.setContactPerson(request.contactPerson());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setActive(true);

        Supplier savedSupplier = supplierRepository.save(supplier);

        return toResponse(savedSupplier);
    }

    public List<SupplierResponse> getAllSuppliers(
            boolean activeOnly
    ) {

        List<Supplier> suppliers = activeOnly
                ? supplierRepository.findByActiveTrue()
                : supplierRepository.findAll();

        return suppliers.stream()
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponse getSupplierById(Long id) {

        return toResponse(findSupplier(id));
    }

    @Transactional
    public SupplierResponse updateSupplier(
            Long id,
            SupplierUpdateRequest request
    ) {

        Supplier supplier = findSupplier(id);

        String name = request.name().trim();

        if (supplierRepository
                .existsByNameIgnoreCaseAndIdNot(name, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Supplier name already exists"
            );
        }

        supplier.setName(name);
        supplier.setContactPerson(request.contactPerson());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());

        supplierRepository.flush();

        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse deactivateSupplier(Long id) {

        Supplier supplier = findSupplier(id);

        supplier.setActive(false);

        supplierRepository.flush();

        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse activateSupplier(Long id) {

        Supplier supplier = findSupplier(id);

        supplier.setActive(true);

        supplierRepository.flush();

        return toResponse(supplier);
    }

    private Supplier findSupplier(Long id) {

        return supplierRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Supplier not found"
                        )
                );
    }

    private SupplierResponse toResponse(Supplier supplier) {

        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}