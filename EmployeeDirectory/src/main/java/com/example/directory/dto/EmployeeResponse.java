package com.example.directory.dto;

import java.time.LocalDate;
import java.util.Set;

public record EmployeeResponse(Long id, String name, String email, String designation,
                               LocalDate joiningDate, Long departmentId, String departmentName,
                               Set<SkillResponse> skills) {
}
