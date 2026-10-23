package com.arunya.smartstock.department.dto;

import com.arunya.smartstock.department.entity.Department;

import java.time.OffsetDateTime;

public record DepartmentResponse(

        Long id,
        Long storeId,
        String departmentCode,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {

    public static DepartmentResponse from(Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getStore().getId(),
                department.getDepartmentCode(),
                department.getName(),
                department.getDescription(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}