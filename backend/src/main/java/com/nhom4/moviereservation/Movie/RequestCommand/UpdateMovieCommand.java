package com.nhom4.moviereservation.Movie.RequestCommand;

import com.nhom4.moviereservation.model.enums.MovieType;

public class UpdateMovieCommand {
   private MovieType movieType;

   public UpdateMovieCommand(){}

   public UpdateMovieCommand(MovieType movieType) {
      this.movieType = movieType;
   }

   // Getters and Setters
   public MovieType getMovieType() {
      return movieType;
   }

   public void setMovieType(MovieType movieType) {
      this.movieType = movieType;
   }
}
