package com.nhom4.moviereservation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.nhom4.moviereservation.model.Genre;
import com.nhom4.moviereservation.service.GenreService;

@RestController
@RequestMapping("/admin/genre")
@CrossOrigin(origins = "*")
public class GenreController {

    @Autowired
    private GenreService genreService;

    // CREATE (POST)
    @PostMapping
    public ResponseEntity<?> createGenre(@RequestBody Genre genre) {
        Genre savedGenre = genreService.createGenre(genre);
    
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

    // READ ALL (GET)
    @GetMapping
    public List<Genre> getAllGenres() {
        return genreService.findAllGenres();
    }

    // READ ONE (GET /{id})
    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenre(@PathVariable Integer id) {
        Optional<Genre> genre = genreService.getGenre(id);
        return genre.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // UPDATE (PUT /{id})
    public ResponseEntity<?> updateGenre(@PathVariable Integer id, @RequestBody Genre genreDetails) {
        Genre updated = genreService.updateGenre(id, genreDetails);
        Map<String, Object> response = new HashMap<>();
        if (updated != null) {
            response.put("Rows matched", 1);
            response.put("Changed", 1);
            response.put("Warnings", 0);
            return ResponseEntity.ok(response);
        }
        else {
            response.put("Rows matched", 0);
            response.put("Changed", 0);
            response.put("Warnings", 1);
            return ResponseEntity.status(404).body(response);
        }
    }


    // DELETE (DELETE /{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}