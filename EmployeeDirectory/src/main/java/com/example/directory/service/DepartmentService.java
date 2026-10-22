package com.example.directory.service;

import com.example.directory.dto.DepartmentRequest;
import com.example.directory.dto.DepartmentResponse;
import com.example.directory.entity.Department;
import com.example.directory.exception.ConflictException;
import com.example.directory.exception.NotFoundException;
import com.example.directory.repository.DepartmentRepository;
import com.example.directory.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository departments;
    private final EmployeeRepository employees;

    public DepartmentService(DepartmentRepository departments, EmployeeRepository employees) {
        this.departments = departments;
        this.employees = employees;
    }

    public DepartmentResponse create(DepartmentRequest request) {
        ensureUniqueName(request.name(), null);
        return response(departments.save(new Department(request.name().trim(), request.location())));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departments.findAll().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return response(getDepartment(id));
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getDepartment(id);
        ensureUniqueName(request.name(), id);
        department.setName(request.name().trim());
        department.setLocation(request.location());
        return response(department);
    }

    public void delete(Long id) {
        Department department = getDepartment(id);
        if (employees.existsByDepartmentId(id))
            throw new ConflictException("Department has employees and cannot be deleted");
        departments.delete(department);
    }

    public Department getDepartment(Long id) {
        return departments.findById(id).orElseThrow(() -> new NotFoundException("Department " + id + " was not found"));
    }

    private void ensureUniqueName(String name, Long currentId) {
        boolean duplicate = currentId == null ? departments.existsByNameIgnoreCase(name.trim())
                : departments.existsByNameIgnoreCaseAndIdNot(name.trim(), currentId);
        if (duplicate) throw new ConflictException("A department with that name already exists");
    }

    private DepartmentResponse response(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getLocation(),
                (int) employees.countByDepartmentId(department.getId()));
    }
}
