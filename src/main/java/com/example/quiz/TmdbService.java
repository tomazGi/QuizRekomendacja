package com.example.quiz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Service
public class TmdbService {

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String apiKey;

    public TmdbService(WebClient webClient) {
        this.webClient = webClient;
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
        int randomPage = new Random().nextInt(500) + 1;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/discover/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "pl-PL")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("page", randomPage)
//                        .queryParam("with_genres", 28)//gatunek
                        .queryParam("primary_release_year", 2000) // rok
//                        .queryParam("vote_average.gte", 5)//ocena
                        .build())
                .retrieve()
                .bodyToMono(MovieResponse.class)
                .map(response -> {
                    List<Movie> movies = response.getMovies();
                    return movies.get(new Random().nextInt(movies.size()));
                });
    }
}
