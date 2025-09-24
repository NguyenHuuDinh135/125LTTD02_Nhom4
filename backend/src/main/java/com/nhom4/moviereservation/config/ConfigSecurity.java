package com.nhom4.moviereservation.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration // Đánh dấu lớp này là một lớp cấu hình
@EnableWebSecurity // Kích hoạt cấu hình bảo mật web của Spring Security
@EnableMethodSecurity // Cho phép sử dụng các chú thích bảo mật trên các phương thức ở tầng dịch vụ
public class ConfigSecurity {

   // Danh sách các endpoint liên quan đến Swagger được phép truy cập mà không cần xác thực
   private static final String[] SWAGGER_WHITELIST = new String[] {
      "/v3/api-docs",
      "/v3/api-docs/**",
      "/swagger-ui.html",
      "/swagger-ui/**"
   };

   // Cấu hình chuỗi bộ lọc bảo mật
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable()) // Tắt cấu hình CSRF cho các API RESTful
            .authorizeHttpRequests(auth -> auth
               .requestMatchers(SWAGGER_WHITELIST).permitAll()
               .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
               .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
               .anyRequest().authenticated()
            )
            
            .httpBasic(Customizer.withDefaults()) // Sử dụng xác thực HTTP Basic
            .formLogin(form -> form.disable()) // Vô hiệu hóa form login mặc định của Spring Security
            
            .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Sử dụng chính sách phiên không trạng thái, tránh dùng session để lưu trữ thông tin người dùng

      return http.build();
   }

   // @Bean
   // public PasswordEncoder passwordEncoder() {
   //    return new BCryptPasswordEncoder();
   // }

}


