

package com.nhom4.moviereservation.service;

import com.nhom4.moviereservation.model.User;
import com.nhom4.moviereservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class SignIn {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");
    // kiểm tra giống như trên
    

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // kiểm tra password
            if (password.equals(user.getPassword())) {
                return ResponseEntity.ok("Đăng nhập thành công");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Sai mật khẩu");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Không tìm thấy người dùng");
        }
    }
}

