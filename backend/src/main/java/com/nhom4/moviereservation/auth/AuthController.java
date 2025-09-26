
package com.nhom4.moviereservation.auth;

import com.nhom4.moviereservation.model.User;

import lombok.RequiredArgsConstructor;

import com.nhom4.moviereservation.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import jakarta.validation.Valid;;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private final AuthService authService;
    
    // Đăng nhập
    // @PostMapping("/login")
    // public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
    //     String email = payload.get("email");
    //     String password = payload.get("password");
    //     Optional<User> user = authService.login(email, password);
    //     if (user.isPresent()) {
    //         return ResponseEntity.ok("Đăng nhập thành công!!");
    //     } else {
    //         return ResponseEntity.status(401).body("Email hoặc mật khẩu sai");
    //     }
    // }
    //Dăng nhâok
    @PostMapping(value = "login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request)
    {   
        return authService.login(request);
    }
    // Đăng ký
    @PostMapping(value = "register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}

