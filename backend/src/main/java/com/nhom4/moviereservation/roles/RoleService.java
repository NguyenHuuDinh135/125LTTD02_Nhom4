package com.nhom4.moviereservation.roles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.model.MovieRole;
import com.nhom4.moviereservation.model.Role;
import com.nhom4.moviereservation.repository.MovieRepository;
import com.nhom4.moviereservation.repository.MovieRoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final MovieRoleRepository movieRoleRepository;
    private final MovieRepository movieRepository;

    public RoleService(RoleRepository roleRepository,
                       MovieRoleRepository movieRoleRepository,
                       MovieRepository movieRepository) {
        this.roleRepository = roleRepository;
        this.movieRoleRepository = movieRoleRepository;
        this.movieRepository = movieRepository;
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> getById(Integer id) {
        return roleRepository.findById(id);
    }

    public Role create(Role role) {
        return roleRepository.save(role);
    }

    public Role update(Integer id, Role roleDetails) {
    return roleRepository.findById(id)
        .map(existing -> {
            // Chỉ cập nhật nếu field không null
            if (roleDetails.getFullName() != null) {
                existing.setFullName(roleDetails.getFullName());
            }
            if (roleDetails.getAge() != null) {
                existing.setAge(roleDetails.getAge());
            }
            if (roleDetails.getPictureUrl() != null) {
                existing.setPictureUrl(roleDetails.getPictureUrl());
            }
            // Lưu bản ghi đã chỉnh sửa
            return roleRepository.save(existing);
        })
        .orElse(null);
}


    public Role delete(Integer id) {
        return roleRepository.findById(id)
            .map(role -> {
                roleRepository.delete(role);
                return role;
            })
            .orElse(null);
    }

    public Map<String, Object> getRoleWithMovies(Integer id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return null;
        }

        Role role = roleOpt.get();
        List<MovieRole> movieRoles = movieRoleRepository.findByRoleId(id);

        // Lấy danh sách movieId
        List<Integer> movieIds = movieRoles.stream()
            .map(mr -> mr.getId().getMovieId())
            .distinct()
            .collect(Collectors.toList());

        // Lấy tất cả phim theo ID
        List<Movie> movies = movieRepository.findAllById(movieIds);

        // Tạo map movieId → Movie để tra cứu nhanh
        Map<Integer, Movie> movieMap = movies.stream()
            .collect(Collectors.toMap(Movie::getId, m -> m));

        // Xây dựng danh sách kết quả: mỗi phần tử gồm movie + role_type
        List<Map<String, Object>> movieList = movieRoles.stream()
            .map(movieRole -> {
                Integer movieId = movieRole.getId().getMovieId();
                Movie movie = movieMap.get(movieId);

                if (movie == null) {
                    return null; // hoặc bỏ qua
                }

                Map<String, Object> movieWithRole = new HashMap<>();
                movieWithRole.put("movie_id", movie.getId());
                movieWithRole.put("role_type", movieRole.getRoleType()); // ← Lấy từ MovieRole!
                movieWithRole.put("title", movie.getTitle());
                movieWithRole.put("summary", movie.getSummary());
                movieWithRole.put("year", movie.getYear());
                movieWithRole.put("rating", movie.getRating());
                movieWithRole.put("trailer_url", movie.getTrailerUrl() != null ? movie.getTrailerUrl().trim() : null);
                movieWithRole.put("poster_url", movie.getPosterUrl() != null ? movie.getPosterUrl().trim() : null);
                movieWithRole.put("movie_type", movie.getMovieType());

                return movieWithRole;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("role_id", role.getId());
        response.put("full_name", role.getFullName());
        response.put("age", role.getAge());
        response.put("picture_url", role.getPictureUrl() != null ? role.getPictureUrl().trim() : null);
        response.put("movies", movieList);

        return response;
    }
}