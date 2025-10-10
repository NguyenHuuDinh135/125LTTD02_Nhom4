package com.nhom4.moviereservation.Movie.RequestCommand; 
import java.math.BigDecimal;
import java.util.List;

// **********************
// Tạm thời chữa xử lý
// **********************
public class CreateMovieCommand {

   private String title;
   private Integer year;
   private String summary;
   private String trailer_url; // hoặc trailerUrl nếu bạn muốn camelCase
   private String poster_url;  // hoặc posterUrl nếu bạn muốn camelCase
   private BigDecimal rating;
   private List<MovieRoleDto> roles;
   private List<Integer> genres; // Danh sách ID thể loại
   private String movie_type;    // hoặc movieType nếu bạn muốn camelCase

   // Constructors
   public CreateMovieCommand() {}

   public CreateMovieCommand(String title, Integer year, String summary, String trailer_url,
                        String poster_url, BigDecimal rating, List<MovieRoleDto> roles,
                        List<Integer> genres, String movie_type) {
      this.title = title;
      this.year = year;
      this.summary = summary;
      this.trailer_url = trailer_url;
      this.poster_url = poster_url;
      this.rating = rating;
      this.roles = roles;
      this.genres = genres;
      this.movie_type = movie_type;
   }

   // Getters and Setters
   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public Integer getYear() {
      return year;
   }

   public void setYear(Integer year) {
      this.year = year;
   }

   public String getSummary() {
      return summary;
   }

   public void setSummary(String summary) {
      this.summary = summary;
   }

   public String getTrailer_url() {
      return trailer_url;
   }

   public void setTrailer_url(String trailer_url) {
      this.trailer_url = trailer_url;
   }

   public String getPoster_url() {
      return poster_url;
   }

   public void setPoster_url(String poster_url) {
      this.poster_url = poster_url;
   }

   public BigDecimal getRating() {
      return rating;
   }

   public void setRating(BigDecimal rating) {
      this.rating = rating;
   }

   public List<MovieRoleDto> getRoles() {
      return roles;
   }

   public void setRoles(List<MovieRoleDto> roles) {
      this.roles = roles;
   }

   public List<Integer> getGenres() {
      return genres;
   }

   public void setGenres(List<Integer> genres) {
      this.genres = genres;
   }

   public String getMovie_type() {
      return movie_type;
   }

   public void setMovie_type(String movie_type) {
      this.movie_type = movie_type;
   }

   // Inner class cho Role
   public static class MovieRoleDto {
      private String role_id;
      private String role_type;

      public MovieRoleDto() {}

      public MovieRoleDto(String role_id, String role_type) {
            this.role_id = role_id;
            this.role_type = role_type;
      }

      // Getters and Setters
      public String getRole_id() {
            return role_id;
      }

      public void setRole_id(String role_id) {
            this.role_id = role_id;
      }

      public String getRole_type() {
            return role_type;
      }

      public void setRole_type(String role_type) {
            this.role_type = role_type;
      }
   }
}