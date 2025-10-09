package com.nhom4.moviereservation.Movie;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom4.moviereservation.Movie.RequestCommand.CreateMovieCommand;
import com.nhom4.moviereservation.model.Genre;
import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.model.MovieGenre;
import com.nhom4.moviereservation.model.MovieRole;
import com.nhom4.moviereservation.model.Role;
import com.nhom4.moviereservation.model.enums.MovieType;
import com.nhom4.moviereservation.repository.GenreRepository;
import com.nhom4.moviereservation.repository.MovieGenreRepository;
import com.nhom4.moviereservation.repository.MovieRoleRepository;
import com.nhom4.moviereservation.repository.RoleRepository;


@Service
public class MovieService {
   @Autowired
   private MovieRepository movieRepository;
   private GenreRepository genreRepository;
   private RoleRepository roleRepository;
   private MovieGenreRepository movieGenreRepository;
   private MovieRoleRepository movieRoleRepository;

   public MovieService(MovieRepository movieRepository, GenreRepository genreRepository, MovieGenreRepository movieGenreRepository, MovieRoleRepository movieRoleRepository, RoleRepository roleRepository) {
      this.movieRepository = movieRepository;
      this.genreRepository = genreRepository;
      this.movieGenreRepository = movieGenreRepository;
      this.movieRoleRepository = movieRoleRepository;
      this.roleRepository = roleRepository;
   }

   public Map<String, Object> findAll() {

      List<Movie> movies = movieRepository.findAll();

      // Duyệt qua từng phim và lấy danh sách thể loại tương ứng
      List<Map<String, Object>> movieList = movies.stream().map(movie -> {
         // Lấy tất cả MovieGenre của phim hiện tại
         List<MovieGenre> movieGenres = movieGenreRepository.findByMovieId(movie.getId());

         // Lấy danh sách thể loại (genre_id, genre)
         List<Map<String, Object>> genres = movieGenres.stream().map(mg -> {
               Genre genre = genreRepository.findById(mg.getId().getGenreId()).orElse(null);
               if (genre == null) return null;
               Map<String, Object> genreMap = new HashMap<>();
               genreMap.put("genre_id", genre.getId());
               genreMap.put("genre", genre.getGenre());
               return genreMap;
         }).filter(Objects::nonNull).collect(Collectors.toList());

         // Gộp thông tin phim + thể loại
         Map<String, Object> movieMap = new LinkedHashMap<>();
         movieMap.put("movie_id", movie.getId());
         movieMap.put("title", movie.getTitle());
         movieMap.put("summary", movie.getSummary());
         movieMap.put("year", movie.getYear());
         movieMap.put("rating", movie.getRating());
         movieMap.put("trailer_url", movie.getTrailerUrl());
         movieMap.put("poster_url", movie.getPosterUrl());
         movieMap.put("movie_type", movie.getMovieType());
         movieMap.put("genres", genres);
         return movieMap;
      }).collect(Collectors.toList());

      // Gói tất cả vào "body"
      Map<String, Object> response = new HashMap<>();
      response.put("body", movieList);
      return response;
   }


   public Optional<Movie> findById(Integer id) {
      return movieRepository.findById(id);
   }

   public Map<String, Object> findByFilter(MovieType movieRequest) {
      List<Movie> movies = movieRepository.findByMovieType(movieRequest);

      // Gói tất cả vào "body"
      Map<String, Object> response = new HashMap<>();
      response.put("body", movies);

      return response;
   }

   public Map<String, Object> findByMovieRole(Integer id) {
      List<MovieRole> movieRoles = movieRoleRepository.findByMovieId(id);

      List<Map<String, Object>> roles = movieRoles.stream().map(mr -> {
            Role role = mr.getRole();
            if (role == null) return null;
            Map<String, Object> genreMap = new HashMap<>();
            genreMap.put("role_id", role.getId());
            genreMap.put("full_name", role.getFullName());
            genreMap.put("age", role.getAge());
            genreMap.put("picture_url", role.getPictureUrl());
            genreMap.put("role_type", mr.getRoleType());
            return genreMap;
         })
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
      
      // Gói tất cả vào "body"
      Map<String, Object> response = new HashMap<>();
      response.put("body", roles);

      return response;
   }

   public Movie create(CreateMovieCommand movieDto) {
      Movie movie = new Movie();
      movie.setTitle(movieDto.getTitle());
      movie.setSummary(movieDto.getSummary());
      movie.setYear(movieDto.getYear());
      movie.setRating(movieDto.getRating());
      movie.setTrailerUrl(movieDto.getTrailer_url());
      movie.setPosterUrl(movieDto.getPoster_url());

      return movieRepository.save(movie);

      // Tạm thời tạo Movie chưa insert các row phụ cho Role và Genre

      // MovieRole movieRole = new MovieRole();
      // movieRole.setId(null);;

      // return movieRepository.save(movie);
   }

   public Movie updateMovie(Integer id, Movie movie) {
      if(movieRepository.existsById(id)) {
         movie.setId(id);
         return movieRepository.save(movie);
      }
      
      return null;
   }

   public void delete(Integer id) {
      movieRepository.deleteById(id);
   }
}
