package com.example.directory.controller;

import com.example.directory.dto.SkillRequest;
import com.example.directory.dto.SkillResponse;
import com.example.directory.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillService skills;

    public SkillController(SkillService skills) {
        this.skills = skills;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@Valid @RequestBody SkillRequest request) {
        return skills.create(request);
    }

    @GetMapping
    public List<SkillResponse> findAll() {
        return skills.findAll();
    }
}
