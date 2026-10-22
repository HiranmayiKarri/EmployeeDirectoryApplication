package com.example.directory.controller;

import com.example.directory.dto.EmployeeRequest;
import com.example.directory.dto.EmployeeResponse;
import com.example.directory.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employees;

    public EmployeeController(EmployeeService employees) {
        this.employees = employees;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeRequest request) {
        return employees.create(request);
    }

    @GetMapping
    public Page<EmployeeResponse> findAll(@RequestParam(required = false) Long departmentId,
                                          @RequestParam(defaultValue = "0") int page, @RequestParam(required = false) Integer size,
                                          @RequestParam(defaultValue = "name") String sort) {
        return employees.findAll(departmentId, page, size, sort);
    }

    @GetMapping("/{id}")
    public EmployeeResponse findById(@PathVariable Long id) {
        return employees.findById(id);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return employees.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        employees.delete(id);
    }

    @PostMapping("/{id}/skills/{sid}")
    public EmployeeResponse addSkill(@PathVariable Long id, @PathVariable Long sid) {
        return employees.addSkill(id, sid);
    }

    @DeleteMapping("/{id}/skills/{sid}")
    public EmployeeResponse removeSkill(@PathVariable Long id, @PathVariable Long sid) {
        return employees.removeSkill(id, sid);
    }
}
