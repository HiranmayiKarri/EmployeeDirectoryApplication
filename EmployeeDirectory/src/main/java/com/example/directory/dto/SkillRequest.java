package com.example.directory.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(@NotBlank String name, String category) {
}
