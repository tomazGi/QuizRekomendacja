package com.example.quiz;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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

    public Mono<MovieResponse> getPopularMovies(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("page", page)
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
//genreId, year, lang, longth, page
    public Mono<MovieResponse> advancedSearch(String genreId,
                                              String year, Optional<String> lang, String time, int page) {


        int yearInt = Integer.parseInt(year);
        int timeInt = Integer.parseInt(time);
        System.out.println(year+" "+timeInt+" "+lang+" "+page);
        return webClient.get()
                .uri(uriBuilder ->  {
            var builder = uriBuilder
                        .path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("with_original_language", lang)
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("page", page)
                        .queryParam("with_genres", genreId)//gatunek
                        .queryParam("primary_release_date.gte", getRangeYear(yearInt).start() + "-01-01")
                        .queryParam("primary_release_date.lte", getRangeYear(yearInt).end() + "-12-31")
//                        .queryParam("with_original_language", lang)
                        .queryParam("with_runtime.gte", getRangeTime(timeInt).start())
                        .queryParam("with_runtime.lte", getRangeYear(timeInt).end())
                        .queryParam("type","movie");

                    lang.ifPresent(language -> builder.queryParam("with_original_language", language));
                        return builder.build();
                })
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

    public Range getRangeYear(int time){
        Range yearRange = new Range(0, 0);
        switch (Integer.valueOf(time)) {
            case 1:
                yearRange = new Range(1900, 2000);
                break;
            case 2:
                yearRange = new Range(2001, 2015);
                break;
            case 3:
                yearRange = new Range(2016, 2020);
                break;
            case 4:
                yearRange = new Range(2021, 2025);
                break;

        }
        return yearRange;
    }

    public Range getRangeTime(int year){
        Range timeRange = new Range(0, 0);
        switch (Integer.valueOf(year)) {
            case 1:
                timeRange = new Range(0, 90);
                break;
            case 2:
                timeRange = new Range(91, 120);
                break;
            case 3:
                timeRange = new Range(120, 400);
                break;
            case 4:
                timeRange = new Range(0, 400);
                break;
        }
        return timeRange;
    }
}
