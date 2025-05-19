package com.example.quiz;

import com.example.quiz.Movie;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MovieResponse {

    private int page;

    @JsonProperty("results")
    private List<Movie> movies;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_results")
    private int totalResults;


}

