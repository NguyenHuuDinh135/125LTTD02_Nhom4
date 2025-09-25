
package com.nhom4.moviereservation.auth;


import com.nhom4.moviereservation.model.User;
import com.nhom4.moviereservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.nhom4.moviereservation.model.User;
import com.nhom4.moviereservation.model.enums.Role;
import com.nhom4.moviereservation.model.enums.UserRole;
import com.nhom4.moviereservation.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDetails user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String role = user.getAuthorities().stream().findFirst().map(grantedAuthority -> grantedAuthority.getAuthority()).orElse("api_user");
        return AuthResponse.builder()
            .email(user.getUsername())
            .role(role)
            .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullname())
            .address(request.getAddress())
            .role(UserRole.API_USER)
            .build();
        userRepository.save(user);
        
        return AuthResponse.builder()
            .email(user.getUsername())
            .role(user.getRole().name())
            .build();
    }
//    @Autowired
//    private PasswordEncoder passwordEncoder;

    // public Optional<User> login(String email, String password) {
    //     Optional<User> userOpt = userRepository.findByEmail(email);
    //     if (userOpt.isPresent()) {
    //         User user = userOpt.get();
    //         //if (passwordEncoder.matches(rawPassword, user.getPassword())) {
    //         if (password.equals(user.getPassword())) {
    //             return Optional.of(user);
    //         }
    //     }
    //     return Optional.empty();
    // }
}
