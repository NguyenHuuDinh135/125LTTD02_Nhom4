package com.nhom4.moviereservation.service;

import com.nhom4.moviereservation.model.Role;
import com.nhom4.moviereservation.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public Role getById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    public Role create(Role role) {
        return roleRepository.save(role);
    }

    public Role update(Integer id, Role role) {
        Role existing = roleRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setFullName(role.getFullName());
            existing.setAge(role.getAge());
            existing.setPictureUrl(role.getPictureUrl());
            return roleRepository.save(existing);
        }
        return null;
    }

    public Role delete(Integer id) {
        Role role = roleRepository.findById(id).orElse(null);;
        roleRepository.delete(role);
        return role;
    }
}
