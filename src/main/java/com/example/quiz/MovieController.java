package com.example.quiz;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.ui.Model;

@Controller
public class MovieController {

    private final TmdbService tmdbService;

    public MovieController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/movies")
    public String showMovies(Model model) {
        MovieResponse response = tmdbService.getPopularMovies().block(); // UWAGA: .block() tylko w kontrolerze MVC
        model.addAttribute("movies", response.getMovies());
        return "movies"; // Szablon: templates/movies.html
    }

    @GetMapping("/random-movie")
    public String showRandomMovie(Model model) {
        Movie randomMovie = tmdbService.getRandomMovie().block(); // blokujemy tylko tu!
        model.addAttribute("movie", randomMovie);
        return "random"; // → szablon random.html
    }
}