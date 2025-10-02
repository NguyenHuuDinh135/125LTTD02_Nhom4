package com.nhom4.moviereservation.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.nhom4.moviereservation.repository.GenreRepository;
import com.nhom4.moviereservation.model.Genre;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> findAllGenres() {
        return genreRepository.findAll();
    }

    public Optional<Genre> getGenre(Integer id) {
        return genreRepository.findById(id);
    }

    public Genre createGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public Genre updateGenre(Integer id, Genre genreDetails) {
        if (genreRepository.existsById(id)) {
            genreDetails.setId(id);
            return genreRepository.save(genreDetails);
        }
        return null;
    }

    public void deleteGenre(Integer id) {
        genreRepository.deleteById(id);
    }
}