package com.nhom4.moviereservation.service;


import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.nhom4.moviereservation.repository.MovieRepository;
import com.nhom4.moviereservation.repository.GenreRepository;
import com.nhom4.moviereservation.model.Genre;
import com.nhom4.moviereservation.model.Movie;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }
    
    public List<Movie> findByGenreId(Integer genreId) {
        return movieRepository.findByGenreId(genreId);
    }
}