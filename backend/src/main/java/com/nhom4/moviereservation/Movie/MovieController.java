package com.nhom4.moviereservation.Movie;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhom4.moviereservation.Movie.RequestCommand.CreateMovieCommand;
import com.nhom4.moviereservation.Movie.RequestCommand.UpdateMovieCommand;
import com.nhom4.moviereservation.model.Movie;
import com.nhom4.moviereservation.model.enums.MovieType;

@RestController
@RequestMapping("/movies")
@CrossOrigin(origins = "*")
public class MovieController {
   @Autowired
   private MovieService movieService;

   @GetMapping
   public ResponseEntity<?> getAllMovies() {
      try {
         Map<String, Object> responseBody = movieService.findAll();

         if (responseBody != null) {
            return ResponseEntity.ok(responseBody);
         } else {
            return ResponseEntity.notFound().build();
         }
      } catch (Exception e) {
         Map<String, String> errorResponse = new HashMap<>();
         errorResponse.put("error", "An error occurred while fetching movies");
         errorResponse.put("details", e.getMessage());
         return ResponseEntity.status(500).body(errorResponse);
      }
   }

   @GetMapping("/id/{id}")
   public ResponseEntity<Movie> getMovieById(@PathVariable Integer id) {
      Optional<Movie> movie = movieService.findById(id);

      return movie.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
   }

   @GetMapping("/filtered")
   public ResponseEntity<?> getMovieByFiltered(@RequestParam(required = false) MovieType movieType) {
      try {
         Map<String, Object> responseBody = movieService.findByFilter(movieType);
         if(responseBody != null)
            return ResponseEntity.ok(responseBody);
         else
            return ResponseEntity.notFound().build();
      } catch (Exception e) {
         Map<String, String> errorResponse = new HashMap<>();
         errorResponse.put("error", "An error occurred while fetching movies");
         errorResponse.put("details", e.getMessage());
         return ResponseEntity.status(500).body(errorResponse);
      }
   }

   @GetMapping("id/{id}/roles")
   public ResponseEntity<?> getAllRolesByMovieId(@PathVariable Integer id) {
      try {
         Map<String, Object> responseBody = movieService.findByMovieRole(id);

         if(responseBody.get("body") == null || responseBody.get("body") instanceof List<?> list && list.isEmpty())
            return ResponseEntity.notFound().build();
         else
            return ResponseEntity.ok(responseBody);
      } catch (Exception e) {
         Map<String, String> errorResponse = new HashMap<>();
         errorResponse.put("error", "An error occurred while fetching movies");
         errorResponse.put("details", e.getMessage());
         return ResponseEntity.status(500).body(errorResponse);
      }
   }

   @PostMapping
   public ResponseEntity<?> createMovie(@RequestBody CreateMovieCommand movie) {

      Movie resultMovie = movieService.create(movie);

      Map<String, Object> response = new LinkedHashMap<>();
      if(resultMovie != null) {
         response.put("movie_id", resultMovie.getId());
         response.put("affected_rows", 1);
         return ResponseEntity.ok(response);
      }else{
         response.put("movie_id", 0);
         response.put("affected_rows", 0); 
         return ResponseEntity.status(500).body(response);
      }

   }

   @PutMapping("id/{id}")
   public ResponseEntity<?> updateMovie(@PathVariable Integer id, @RequestBody UpdateMovieCommand requestBody) {
      
      Map<String, Object> response = new HashMap<>();
      
      try {
         MovieType movieTypeRequest = requestBody.getMovieType();
         
         if (movieTypeRequest == null) {
            response.put("error", "Movie type is required for update.");
            response.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
         }
         
         Optional<Movie> movieById = movieService.findById(id);
         if (!movieById.isPresent()) {
            response.put("error", "Movie not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
         }
         
         Movie movieUpdate = new Movie();
         movieUpdate.setMovieType(movieTypeRequest);
         
         Movie resultUpdate = movieService.updateMovie(id, movieUpdate);
         
         if (resultUpdate != null) {
            response.put("Rows matched", 1);
            response.put("Changed", 1);
            response.put("Warnings", 0);
            return ResponseEntity.ok(response);
         } else {
            response.put("Rows matched", 0);
            response.put("Changed", 0);
            response.put("Warnings", 1);
            response.put("error", "Update failed");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
         }
         
      } catch (DataIntegrityViolationException e) {
         // Lỗi database constraint (foreign key, unique, etc.)
         response.put("error", "Database constraint violation");
         response.put("message", "Cannot update movie due to data integrity issues");
         response.put("details", e.getMostSpecificCause().getMessage());
         response.put("status", HttpStatus.CONFLICT.value());
         return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      }
   }

   @DeleteMapping("id/{id}")
   public ResponseEntity<?> deleteMovie(@PathVariable Integer id) {
      try {
         Optional<Movie> movieById = movieService.findById(id);
         return movieById.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
      } catch (Exception e) {
         Map<String, Object> response = new HashMap<>();
         response.put("Changed", 0);
         response.put("Rows matched", 1);
         response.put("Status", "Erorr");
         return ResponseEntity.status(500)
            .body(response);
      }
   }
}
