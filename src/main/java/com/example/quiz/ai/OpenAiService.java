package com.example.quiz.ai;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class OpenAiService {

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String apiKey;

    public OpenAiService(WebClient webClient) {
        this.webClient = webClient;
    }


    public List<Question> generateQuestions() {
        return getFallbackQuestions();
    }


    public String getRecommendation(List<String> answers) {
        // Przygotowanie treści polecenia dla modelu na podstawie odpowiedzi
        // Budujemy opis preferencji użytkownika w formie zrozumiałej dla modelu:
        StringBuilder prefs = new StringBuilder();
        prefs.append("Preferencje użytkownika:\n");
        // Mapujemy odpowiedzi do kategorii pytań (zakładając kolejność pytań z generowania)
        // Jeżeli pytania generowane są dynamicznie, można przesłać modelowi zarówno pytanie jak i odpowiedź.
        String[] categories = {"Gatunek", "Nastrój", "Forma", "Najważniejsze", "Era"};
        for (int i = 0; i < answers.size(); i++) {
            String category = (i < categories.length ? categories[i] : "Kategoria" + (i+1));
            prefs.append("- ").append(category).append(": ").append(answers.get(i)).append("\n");
        }
        // Tworzymy polecenie dla modelu, aby zarekomendował film/serial na podstawie powyższych preferencji
        String prompt = prefs.toString() +
                "\nNa podstawie tych preferencji, zaproponuj jeden film lub serial, " +
                "który najlepiej pasuje do gustu użytkownika. Podaj tytuł i krótko uzasadnij swój wybór w jednym akapicie (po polsku).";

        return "result";
    }

    // Metoda zapasowa zwracająca z góry ustalone pytania w razie błędu API
    private List<Question> getFallbackQuestions() {
        List<Question> fallback = new ArrayList<>();
        fallback.add(new Question("Jaki gatunek filmowy lub serialowy lubisz najbardziej?",
                List.of("Akcja", "Komedia", "Dramat", "Science-fiction")));
        fallback.add(new Question("Jaki nastrój preferujesz w filmach/serialach?",
                List.of("Lekki i zabawny", "Mroczny i poważny", "Wzruszający", "Trzymający w napięciu")));
        fallback.add(new Question("Wolisz oglądać filmy czy seriale?",
                List.of("Filmy", "Seriale", "Bez preferencji")));
        fallback.add(new Question("Co jest dla Ciebie najważniejsze w filmie lub serialu?",
                List.of("Ciekawa fabuła", "Bohaterowie", "Gra aktorska", "Strona wizualna")));
        fallback.add(new Question("Czy preferujesz nowości czy klasyki?",
                List.of("Nowe produkcje", "Klasyczne produkcje", "Bez znaczenia")));
        return fallback;
    }
}

