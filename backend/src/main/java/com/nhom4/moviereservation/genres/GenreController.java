package com.nhom4.moviereservation.genres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.nhom4.moviereservation.model.Genre;

@RestController
@RequestMapping("/genres")
@CrossOrigin(origins = "*")
public class GenreController {

    @Autowired
    private GenreService genreService; // Đảm bảo đã inject GenreService

    // --- CREATE (POST) ---
    // Endpoint: POST http://localhost:8080/genres
    // Request Body: {"genre": "Cartoon"}
    // Response Body (Success): {"genre_id": <int>, "affected_rows": <int>}
    @PostMapping
    public ResponseEntity<?> createGenre(@RequestBody Map<String, String> requestBody) {
        String genreName = requestBody.get("genre");
        if (genreName == null || genreName.trim().isEmpty()) {
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("error", "Genre name is required.");
             return ResponseEntity.badRequest().body(errorResponse);
        }

        Genre genreToCreate = new Genre();
        genreToCreate.setGenre(genreName);

        Genre savedGenre = genreService.createGenre(genreToCreate);

        if (savedGenre != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("genre_id", savedGenre.getId());
            response.put("affected_rows", 1);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("genre_id", 0);
            response.put("affected_rows", 0);
            return ResponseEntity.status(500).body(response);
        }
    }

    // --- READ ALL (GET) ---
    // Endpoint: GET http://localhost:8080/genres
    @GetMapping
    public List<Genre> getAllGenres() {
        return genreService.findAllGenres();
    }

    // --- READ ONE (GET /{id}) ---
    // Endpoint: GET http://localhost:8080/genres/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenre(@PathVariable Integer id) {
        Optional<Genre> genre = genreService.getGenre(id);
        return genre.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- FIND MOVIES BY GENRE_ID (GET /{id}/movies) ---
    // Endpoint: GET http://localhost:8080/genres/{id}/movies
    @GetMapping("/{id}/movies")
    public ResponseEntity<?> getAllMoviesByGenreId(@PathVariable Integer id) {
        try {
            // Gọi phương thức từ GenreService để lấy dữ liệu
            Map<String, Object> responseBody = genreService.getGenreWithMovies(id);

            if (responseBody != null) {
                // Trả về phản hồi thành công (200 OK) với body đã định dạng
                return ResponseEntity.ok(responseBody);
            } else {
                // Trả về lỗi 404 nếu không tìm thấy thể loại
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Xử lý lỗi nếu có ngoại lệ xảy ra
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while fetching movies for genre ID: " + id);
            errorResponse.put("details", e.getMessage()); // Chỉ nên đưa chi tiết lỗi trong môi trường dev
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // --- UPDATE (PUT /{id}) ---
    // Endpoint: PUT http://localhost:8080/genres/{id}
    // Request Body: {"genre": "Animated Cartoon"}
    // Response Body (Success): {"Rows matched": 1, "Changed": 1, "Warnings": 0}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable Integer id, @RequestBody Map<String, String> requestBody) {
        String newGenreName = requestBody.get("genre");
        if (newGenreName == null || newGenreName.trim().isEmpty()) {
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("error", "Genre name is required for update.");
             return ResponseEntity.badRequest().body(errorResponse);
        }

        Genre genreDetails = new Genre();
        genreDetails.setGenre(newGenreName);

        Genre updated = genreService.updateGenre(id, genreDetails);
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

    // --- DELETE (DELETE /{id}) ---
    // Endpoint: DELETE http://localhost:8080/genres/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        Optional<Genre> genreOpt = genreService.getGenre(id);
        if (genreOpt.isPresent()) {
            genreService.deleteGenre(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found nếu không tìm thấy
        }
    }
}