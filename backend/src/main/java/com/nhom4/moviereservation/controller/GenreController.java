package com.nhom4.moviereservation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
    public ResponseEntity<Genre> createGenre(@RequestBody Genre genre) {
        return ResponseEntity.ok(genreService.createGenre(genre));
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
    @PutMapping("/{id}")
    public ResponseEntity<Genre> updateGenre(@PathVariable Integer id, @RequestBody Genre genreDetails) {
        Genre updated = genreService.updateGenre(id, genreDetails);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE (DELETE /{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Integer id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}