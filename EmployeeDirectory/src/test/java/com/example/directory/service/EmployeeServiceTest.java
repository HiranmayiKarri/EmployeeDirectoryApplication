package com.example.directory.service;

import com.example.directory.dto.EmployeeRequest;
import com.example.directory.dto.EmployeeResponse;
import com.example.directory.entity.Department;
import com.example.directory.entity.Employee;
import com.example.directory.entity.Skill;
import com.example.directory.exception.ConflictException;
import com.example.directory.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employees;
    @Mock
    private DepartmentService departments;
    @Mock
    private SkillService skills;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(employees, departments, skills, 2, 15);
    }

    @Test
    void createsEmployeeWithDepartmentAndSkills() {
        Department department = new Department("Engineering", "London");
        Skill java = new Skill("Java", "Backend");
        when(employees.existsByEmailIgnoreCase("dev@example.com")).thenReturn(false);
        when(departments.getDepartment(7L)).thenReturn(department);
        when(skills.getSkill(3L)).thenReturn(java);
        when(employees.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse response = service.create(new EmployeeRequest(" Dev ", "dev@example.com", "Engineer",
                LocalDate.of(2024, 1, 15), 7L, Set.of(3L)));

        assertEquals("Dev", response.name());
        assertEquals("Engineering", response.departmentName());
        assertEquals(Set.of("Java"), response.skills().stream().map(skill -> skill.name()).collect(Collectors.toSet()));
        verify(employees).save(any(Employee.class));
    }

    @Test
    void rejectsAddingSkillWhenLimitReached() {
        Department department = new Department("Engineering", "London");
        Employee employee = new Employee("Dev", "dev@example.com", "Engineer", LocalDate.now(), department);
        employee.setSkills(new LinkedHashSet<>(Set.of(new Skill("Java", "Backend"), new Skill("SQL", "Data"))));
        when(employees.findById(1L)).thenReturn(Optional.of(employee));
        when(skills.getSkill(3L)).thenReturn(new Skill("React", "Frontend"));

        assertThrows(ConflictException.class, () -> service.addSkill(1L, 3L));
        assertEquals(2, employee.getSkills().size());
    }

    @Test
    void rejectsDuplicateEmailBeforePersisting() {
        when(employees.existsByEmailIgnoreCase("dev@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(new EmployeeRequest("Dev", "dev@example.com", null,
                LocalDate.now(), 1L, Set.of())));
        verify(employees, never()).save(any());
        verifyNoInteractions(departments, skills);
    }
}
