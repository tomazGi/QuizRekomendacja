package com.example.quiz;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.ui.Model;

import java.util.Optional;

@Controller
public class MovieController {

    private final TmdbService tmdbService;

    public MovieController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/popularMovies")
    public String showMovies( @RequestParam(name = "page", defaultValue = "1") int page,
                              Model model) {
        MovieResponse response = tmdbService.getPopularMovies(page).block();
        model.addAttribute("movies", response.getMovies());
        model.addAttribute("currentPage", response.getPage());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("title", "Popularne filmy:");
        return "popular"; // Szablon: templates/movies.html
    }

    @GetMapping("/random-movie")
    public String showRandomMovie(Model model) {
        Movie randomMovie = tmdbService.getRandomMovie().block(); // blokujemy tylko tu!
        model.addAttribute("movie", randomMovie);
        model.addAttribute("title", "Wylosowany film:");
        return "random"; // → szablon random.html
    }

    @GetMapping("/form")
    public String showForm(Model model) {
        return "form";
    }

    @GetMapping("/search/{genreId}")
    public String searchMoviesGet(
            @RequestParam(name = "genreId", defaultValue = "1") String genreId,
            @RequestParam(name = "year", defaultValue = "1") String year,
            @RequestParam(name = "lang", defaultValue = "1") Optional<String> lang,
            @RequestParam(name = "time", defaultValue = "1") String time,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        MovieResponse response  = tmdbService.advancedSearch(genreId, year, lang, time, page).block();
        model.addAttribute("movies", response.getMovies());
        model.addAttribute("currentPage", response.getPage());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("title", "Wyniki wyszukiwania:");
        return "result";
    }

    //  //genreId, lang, long, year
    @PostMapping("/search")
    public String searchMovies(
            @RequestParam(name = "genreId", defaultValue = "1") String genreId,
            @RequestParam(name = "year", defaultValue = "1") String year,
            @RequestParam(name = "lang", defaultValue = "1") Optional<String> lang,
            @RequestParam(name = "time", defaultValue = "1") String time,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        MovieResponse response  = tmdbService.advancedSearch(genreId, year, lang, time, page).block();
        model.addAttribute("movies", response.getMovies());
        model.addAttribute("currentPage", response.getPage());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("title", "Wyniki wyszukiwania:");
        return "result";
    }

    @GetMapping("/genres/{id}")
    public String showMoviesByGenre(@PathVariable("id") int genreId,
                                    @RequestParam(name = "page", defaultValue = "1") int page,
                                    Model model) {

        MovieResponse response  = tmdbService.getMoviesByGenre(genreId, page).block();
        model.addAttribute("movies", response.getMovies());
        model.addAttribute("currentPage", response.getPage());
        model.addAttribute("totalPages", response.getTotalPages());
        model.addAttribute("genreId", genreId);
        model.addAttribute("title", "Gatunek: "+tmdbService.getGenreNameById(genreId));
        return "movies";
    }

}