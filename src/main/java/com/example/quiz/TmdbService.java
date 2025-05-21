package com.example.quiz;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

@Service
public class TmdbService {

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String apiKey;

    public TmdbService(WebClient webClient) {
        this.webClient = webClient;
    }

    private Map<Integer, String> genreMap = new HashMap<>();

    @PostConstruct
    public void initGenres() {
        List<GenreDto> genres = getGenresFromTmdb();
        genreMap = genres.stream().collect(Collectors.toMap(GenreDto::getId, GenreDto::getName));
    }

    public List<GenreDto> getGenresFromTmdb() {
        GenreListResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/genre/movie/list")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .build())
                .retrieve()
                .bodyToMono(GenreListResponse.class)
                .block();

        return response != null ? response.getGenres() : List.of();
    }

    public String getGenreNameById(int id) {
        return genreMap.getOrDefault(id, "Nieznany");
    }

    public Mono<MovieResponse> getPopularMovies() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("page", "1")
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class);
    }

    public Mono<Movie> getRandomMovie() {
        int randomPage = abs(new Random().nextInt(500) + 1);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("page", randomPage)
//                        .queryParam("with_genres", 28)//gatunek
//                        .queryParam("primary_release_year", 2000) // rok
//                        .queryParam("vote_average.gte", 5)//ocena
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class)
                .map(response -> {
                    List<Movie> movies = response.getMovies();
                    return movies.get(abs(new Random().nextInt(movies.isEmpty() ?1:movies.size())));
                });
    }

    public Mono<MovieResponse> getRecomandation(String genre) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("sort_by", "vote_average.desc")
                        .queryParam("page", "1")
                        .queryParam("include_adult", "false")
                        .queryParam("with_genres", genre)//gatunek
                        .queryParam("primary_release_year", "2000") // rok
//                        .queryParam("vote_average.gte", 5)//ocena
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class);
    }

    public Mono<MovieResponse> advancedSearch(String type, String genreId, Integer year, int page) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("page", page)
                        .queryParam("with_genres", genreId)//gatunek
                        .queryParam("primary_release_year", year) // rok
                        .queryParam("type",type)
//                        .queryParam("vote_average.gte", 5)//ocena
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class);
    }


    public Mono<MovieResponse> getMoviesByGenre(int genreId, int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("page", page)
                        .queryParam("with_genres", genreId)//gatunek

                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class);
    }
}
