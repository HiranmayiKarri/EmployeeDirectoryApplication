package com.example.directory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record EmployeeRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String designation,
        @NotNull LocalDate joiningDate,
        @NotNull Long departmentId,
        Set<@NotNull Long> skillIds) {
}
