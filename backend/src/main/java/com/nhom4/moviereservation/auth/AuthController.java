
package com.nhom4.moviereservation.auth;

import com.nhom4.moviereservation.model.User;
import com.nhom4.moviereservation.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        Optional<User> user = authService.login(email, password);
        if (user.isPresent()) {
            return ResponseEntity.ok("Đăng nhập thành công!!");
        } else {
            return ResponseEntity.status(401).body("Email hoặc mật khẩu sai");
        }
    }
}

