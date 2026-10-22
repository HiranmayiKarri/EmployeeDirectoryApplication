package com.example.directory.service;

import com.example.directory.dto.EmployeeRequest;
import com.example.directory.dto.EmployeeResponse;
import com.example.directory.dto.SkillResponse;
import com.example.directory.entity.Department;
import com.example.directory.entity.Employee;
import com.example.directory.entity.Skill;
import com.example.directory.exception.ConflictException;
import com.example.directory.exception.NotFoundException;
import com.example.directory.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@Transactional
public class EmployeeService {
    private final EmployeeRepository employees;
    private final DepartmentService departments;
    private final SkillService skills;
    private final int maxSkillsPerEmployee;
    private final int defaultPageSize;

    public EmployeeService(EmployeeRepository employees, DepartmentService departments, SkillService skills,
                           @Value("${directory.max-skills-per-employee}") int maxSkillsPerEmployee,
                           @Value("${pagination.default-page-size}") int defaultPageSize) {
        this.employees = employees;
        this.departments = departments;
        this.skills = skills;
        this.maxSkillsPerEmployee = maxSkillsPerEmployee;
        this.defaultPageSize = defaultPageSize;
    }

    public EmployeeResponse create(EmployeeRequest request) {
        ensureUniqueEmail(request.email(), null);
        Department department = departments.getDepartment(request.departmentId());
        Employee employee = new Employee(request.name().trim(), request.email().trim(), request.designation(), request.joiningDate(), department);
        employee.setSkills(resolveSkills(request.skillIds()));
        return response(employees.save(employee));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findAll(Long departmentId, int page, Integer size, String sort) {
        Pageable pageable = pageable(page, size, sort);
        Page<Employee> result = departmentId == null ? employees.findAll(pageable) : employees.findByDepartmentId(departmentId, pageable);
        return result.map(this::response);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return response(getEmployee(id));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = getEmployee(id);
        ensureUniqueEmail(request.email(), id);
        employee.setName(request.name().trim());
        employee.setEmail(request.email().trim());
        employee.setDesignation(request.designation());
        employee.setJoiningDate(request.joiningDate());
        employee.setDepartment(departments.getDepartment(request.departmentId()));
        employee.setSkills(resolveSkills(request.skillIds()));
        return response(employee);
    }

    public void delete(Long id) {
        employees.delete(getEmployee(id));
    }

    public EmployeeResponse addSkill(Long employeeId, Long skillId) {
        Employee employee = getEmployee(employeeId);
        Skill skill = skills.getSkill(skillId);
        if (!employee.getSkills().contains(skill)) {
            if (employee.getSkills().size() >= maxSkillsPerEmployee)
                throw new ConflictException("Employee cannot have more than " + maxSkillsPerEmployee + " skills");
            employee.getSkills().add(skill);
        }
        return response(employee);
    }

    public EmployeeResponse removeSkill(Long employeeId, Long skillId) {
        Employee employee = getEmployee(employeeId);
        Skill skill = skills.getSkill(skillId);
        employee.getSkills().remove(skill);
        return response(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findByDepartment(Long departmentId, int page, Integer size, String sort) {
        departments.getDepartment(departmentId);
        return employees.findByDepartmentId(departmentId, pageable(page, size, sort)).map(this::response);
    }

    private Employee getEmployee(Long id) {
        return employees.findById(id).orElseThrow(() -> new NotFoundException("Employee " + id + " was not found"));
    }

    private Set<Skill> resolveSkills(Set<Long> skillIds) {
        Set<Long> ids = skillIds == null ? Set.of() : skillIds;
        if (ids.size() > maxSkillsPerEmployee)
            throw new ConflictException("Employee cannot have more than " + maxSkillsPerEmployee + " skills");
        Set<Skill> resolved = new LinkedHashSet<>();
        ids.forEach(id -> resolved.add(skills.getSkill(id)));
        return resolved;
    }

    private Pageable pageable(int page, Integer size, String sort) {
        int pageSize = size == null ? defaultPageSize : size;
        if (page < 0 || pageSize < 1 || pageSize > 200)
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 200");
        String sortField = sort == null || sort.isBlank() ? "name" : sort;
        if (!Set.of("name", "email", "joiningDate", "designation").contains(sortField))
            throw new IllegalArgumentException("sort must be one of name, email, joiningDate, designation");
        return PageRequest.of(page, pageSize, Sort.by(sortField).ascending());
    }

    private void ensureUniqueEmail(String email, Long currentId) {
        boolean duplicate = currentId == null ? employees.existsByEmailIgnoreCase(email.trim())
                : employees.existsByEmailIgnoreCaseAndIdNot(email.trim(), currentId);
        if (duplicate) throw new ConflictException("An employee with that email already exists");
    }

    private EmployeeResponse response(Employee employee) {
        Set<SkillResponse> skillResponses = employee.getSkills().stream()
                .map(skill -> new SkillResponse(skill.getId(), skill.getName(), skill.getCategory()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return new EmployeeResponse(employee.getId(), employee.getName(), employee.getEmail(), employee.getDesignation(),
                employee.getJoiningDate(), employee.getDepartment().getId(), employee.getDepartment().getName(), skillResponses);
    }
}
