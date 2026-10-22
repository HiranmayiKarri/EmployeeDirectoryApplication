package com.example.directory.service;

import com.example.directory.dto.SkillRequest;
import com.example.directory.dto.SkillResponse;
import com.example.directory.entity.Skill;
import com.example.directory.exception.ConflictException;
import com.example.directory.exception.NotFoundException;
import com.example.directory.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillService {
    private final SkillRepository skills;

    public SkillService(SkillRepository skills) {
        this.skills = skills;
    }

    public SkillResponse create(SkillRequest request) {
        ensureUniqueName(request.name());
        return response(skills.save(new Skill(request.name().trim(), request.category())));
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> findAll() {
        return skills.findAll().stream().map(this::response).toList();
    }

    public Skill getSkill(Long id) {
        return skills.findById(id).orElseThrow(() -> new NotFoundException("Skill " + id + " was not found"));
    }

    private void ensureUniqueName(String name) {
        if (skills.existsByNameIgnoreCase(name.trim()))
            throw new ConflictException("A skill with that name already exists");
    }

    private SkillResponse response(Skill skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getCategory());
    }
}
