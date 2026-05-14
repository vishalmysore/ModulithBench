package com.benchmark.healthcare.department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department updateDepartment(Long id, Department details) {
        Department dept = getDepartmentById(id);
        dept.setName(details.getName());
        dept.setCode(details.getCode());
        dept.setDescription(details.getDescription());
        dept.setHeadDoctorId(details.getHeadDoctorId());
        return departmentRepository.save(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.delete(getDepartmentById(id));
    }

    @Transactional(readOnly = true)
    public void validateDepartmentExists(Long deptId) {
        if (!departmentRepository.existsById(deptId)) {
            throw new DepartmentNotFoundException(deptId);
        }
    }
}
