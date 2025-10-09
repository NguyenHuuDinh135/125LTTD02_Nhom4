package com.nhom4.moviereservation.controller;

import com.nhom4.moviereservation.model.MovieRole;
import com.nhom4.moviereservation.model.Role;
import com.nhom4.moviereservation.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<Role> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    public Role getById(@PathVariable Integer id) {
        return roleService.getById(id);
    }

    @PostMapping
    public Role create(@RequestBody Role role) {
        return roleService.create(role);
    }

    @PutMapping("/{id}")
    public Role update(@PathVariable Integer id, @RequestBody Role role) {
        return roleService.update(id, role);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Role> delete(@PathVariable Integer id) {
        Role deletedRole = roleService.delete(id);
        return ResponseEntity.ok(deletedRole);
    }
    @GetMapping("/{id}/movies")
    public ResponseEntity<?> getRoleWithMovies(@PathVariable Integer id) {
        return roleService.getRoleWithMovies(id)
                .map(role -> {
                    Map<String, Object> result = new HashMap<>();

                    Map<String, Object> roleInfo = new HashMap<>();
                    roleInfo.put("id", role.getId());
                    roleInfo.put("fullName", role.getFullName());
                    roleInfo.put("age", role.getAge());
                    roleInfo.put("pictureUrl", role.getPictureUrl());
                    result.put("role", roleInfo);

                    List<Map<String, Object>> movies = role.getMovieRoles().stream()
                            .map(movieRole -> {
                                Map<String, Object> movieData = new HashMap<>();
                                movieData.put("id", movieRole.getMovie().getId());
                                movieData.put("title", movieRole.getMovie().getTitle());
                                movieData.put("roleType", movieRole.getRoleType().toString());
                                return movieData;
                            })
                            .toList();

                    result.put("movies", movies);

                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }



}
