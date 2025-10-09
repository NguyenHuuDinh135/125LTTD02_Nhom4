package com.nhom4.moviereservation.roles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom4.moviereservation.model.Role;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<List<Role>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable Integer id) {
        return roleService.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Role> create(@RequestBody Role role) {
        Role saved = roleService.create(role);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    //Chỉ cập nhật mỗi fullname và k có định dạng sẵn
    public ResponseEntity<?> updateRole(@PathVariable Integer id, @RequestBody Map<String, String> requestBody) {
        String fullName = (String) requestBody.get("fullName");

        if (fullName == null || fullName.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Full name is required for update.");
            return ResponseEntity.badRequest().body(error);
        }

        Role roleDetails = new Role();
        roleDetails.setFullName(fullName);

        Role updated = roleService.update(id, roleDetails);

        Map<String, Object> response = new HashMap<>();
        if (updated != null) {
            response.put("Rows matched", 1);
            response.put("Changed", 1);
            response.put("Warnings", 0);
            return ResponseEntity.ok(response);
        } else {
            response.put("Rows matched", 0);
            response.put("Changed", 0);
            response.put("Warnings", 1);
            return ResponseEntity.status(404).body(response);
        }
}
    // Cập nhật đa dụng (Muốn cập nhật bao nhiêu giá trị trong 3 giá trị cũng được)
    // Tuy nhiên do thông báo không khớp nên tạm thời không sử dụng
    /*public ResponseEntity<?> updateRole(@PathVariable Integer id, @RequestBody Role roleDetails) {
        Role updated = roleService.update(id, roleDetails);

        Map<String, Object> response = new HashMap<>();
        if (updated != null) {
            response.put("Rows matched", 1);
            response.put("Changed", 1);
            response.put("Warnings", 0);
            return ResponseEntity.ok(response);
        } else {
            response.put("Rows matched", 0);
            response.put("Changed", 0);
            response.put("Warnings", 1);
            return ResponseEntity.status(404).body(response);
        }
    }*/

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Role deleted = roleService.delete(id);
        if (deleted != null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/movies")
    public ResponseEntity<?> getRoleWithMovies(@PathVariable Integer id) {
        Map<String, Object> responseBody = roleService.getRoleWithMovies(id);
        if (responseBody != null) {
            return ResponseEntity.ok(responseBody);
        }
        return ResponseEntity.notFound().build();
    }
}