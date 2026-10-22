package com.example.directory.controller;

import com.example.directory.dto.DepartmentRequest;
import com.example.directory.dto.DepartmentResponse;
import com.example.directory.dto.EmployeeResponse;
import com.example.directory.service.DepartmentService;
import com.example.directory.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departments;
    private final EmployeeService employees;

    public DepartmentController(DepartmentService departments, EmployeeService employees) {
        this.departments = departments;
        this.employees = employees;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) {
        return departments.create(request);
    }

    @GetMapping
    public List<DepartmentResponse> findAll() {
        return departments.findAll();
    }

    @GetMapping("/{id}")
    public DepartmentResponse findById(@PathVariable Long id) {
        return departments.findById(id);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departments.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        departments.delete(id);
    }

    @GetMapping("/{id}/employees")
    public Page<EmployeeResponse> employees(@PathVariable Long id,
                                            @RequestParam(defaultValue = "0") int page, @RequestParam(required = false) Integer size,
                                            @RequestParam(defaultValue = "name") String sort) {
        return employees.findByDepartment(id, page, size, sort);
    }
}
