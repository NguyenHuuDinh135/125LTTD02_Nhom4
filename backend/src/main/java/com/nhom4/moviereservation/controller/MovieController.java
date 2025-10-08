package com.nhom4.moviereservation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.nhom4.moviereservation.model.Genre;
import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.service.GenreService;
import com.nhom4.moviereservation.service.MovieService;

@RestController
@RequestMapping("/admin/movie")
@CrossOrigin(origins = "*")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @Autowired
    private GenreService genreService;

    //READ for Genreid
    @GetMapping("/by-genre/{genreId}")
    public ResponseEntity<Map<String, Object>> getMoviesByGenre(@PathVariable Integer genreId) {
        Genre genre = genreService.getGenre(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        List<Movie> movies = movieService.findByGenreId(genreId);

        List<Map<String, Object>> movieList = movies.stream().map(movie -> {
            Map<String, Object> movieMap = new LinkedHashMap<>();
            movieMap.put("movie_id", movie.getId());
            movieMap.put("title", movie.getTitle());
            movieMap.put("summary", movie.getSummary());
            movieMap.put("year", movie.getYear());
            movieMap.put("rating", String.valueOf(movie.getRating()));
            movieMap.put("trailer_url", movie.getTrailerUrl());
            movieMap.put("poster_url", movie.getPosterUrl());
            movieMap.put("movie_type", movie.getMovieType().name().toLowerCase());
            return movieMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("genre_id", genre.getId());
        response.put("genre", genre.getGenre());
        response.put("movies", movieList);

        return ResponseEntity.ok(response);
    }
}
