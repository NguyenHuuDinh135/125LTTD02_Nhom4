package com.nhom4.moviereservation.genres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nhom4.moviereservation.model.Genre;
import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.model.MovieGenre;
import com.nhom4.moviereservation.repository.MovieGenreRepository;
import com.nhom4.moviereservation.repository.MovieRepository;

@Service
public class GenreService {
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository; 
     public GenreService(GenreRepository genreRepository, 
                        MovieGenreRepository movieGenreRepository, 
                        MovieRepository movieRepository) {
        this.genreRepository = genreRepository;
        this.movieGenreRepository = movieGenreRepository;
        this.movieRepository = movieRepository;
    }

    public List<Genre> findAllGenres() {
        return genreRepository.findAll();
    }

    // Phương thức mới để lấy thông tin thể loại kèm danh sách phim
    public Map<String, Object> getGenreWithMovies(Integer id) { 
        Optional<Genre> genreOpt = genreRepository.findById(id);
        if (genreOpt.isPresent()) {
            Genre genre = genreOpt.get();
            
            // Gọi MovieGenreRepository để lấy các bản ghi MovieGenre có genreId khớp
            List<MovieGenre> movieGenres = movieGenreRepository.findByGenreId(id); 

            // Trích xuất danh sách movieId từ các MovieGenre
            List<Integer> movieIds = movieGenres.stream()
                                                .map(mg -> mg.getId().getMovieId()) // Giả định MovieGenreId có phương thức getMovieId()
                                                .collect(Collectors.toList());

            // Gọi MovieRepository để lấy danh sách Movie thực tế từ danh sách movieId
            List<Movie> movies = movieRepository.findAllById(movieIds);

            // Tạo Map phản hồi theo cấu trúc mong muốn
            Map<String, Object> response = new HashMap<>();
            response.put("movies", movies);
            response.put("genre", genre.getGenre());
            response.put("genre_id", genre.getId());
            
            return response;
        }
        return null; // Trả về null nếu không tìm thấy thể loại
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