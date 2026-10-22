package com.example.directory.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(@NotBlank String name, String location) {
}
