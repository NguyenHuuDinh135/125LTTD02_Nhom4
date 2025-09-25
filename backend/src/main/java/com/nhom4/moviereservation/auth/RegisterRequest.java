package com.nhom4.moviereservation.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email cannot be empty")
    @Size(min = 3, max = 20, message = "Email must be between 3 and 20 characters")
    String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password;

    @NotBlank(message = "Firstname cannot be empty")
    String fullname;

    @NotBlank(message = "Address cannot be empty")
    String address;
}
