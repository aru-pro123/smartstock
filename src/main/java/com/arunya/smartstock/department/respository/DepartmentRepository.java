package com.arunya.smartstock.department.respository;

import com.arunya.smartstock.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findByStoreId(Long storeId);

    boolean existsByStoreIdAndDepartmentCode(
            Long storeId,
            String departmentCode
    );
}