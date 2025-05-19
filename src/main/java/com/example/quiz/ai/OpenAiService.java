package com.example.quiz.ai;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

@Service
public class OpenAiService {

    // Wstrzykiwanie konfiguracji z application.properties
    @Value("${openai.api.url}")
    private String openAiApiUrl;
    @Value("${openai.api.key}")
    private String openAiApiKey;
    @Value("${openai.api.model}")
    private String openAiModel;

    // Klient HTTP do wysyłania żądań
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generuje listę pytań quizu przy użyciu GPT-4.
     * @return lista obiektów Question (pytanie + opcje)
     */
    public List<Question> generateQuestions() {
        // Treść zapytania (prompt) do modelu GPT-4 - prosimy o 5 pytań z opcjami w formacie JSON
        String prompt = """
            Wygeneruj 5 pytań quizu w formacie JSON (jako tablica obiektów),
            które pomogą określić preferencje filmowe lub serialowe użytkownika.
            Każdy obiekt powinien mieć pola "question" i "options".
            Pytania wielokrotnego wyboru, po polsku, dotyczące takich preferencji jak gatunek, nastrój, forma (film czy serial), itp.
            Odpowiedź zwróć *tylko* jako poprawny JSON bez dodatkowych komentarzy.
            """;

        // Budujemy strukturę zapytania do OpenAI API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiModel);
        // Przygotowanie wiadomości dla chat completion
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        // Opcjonalne parametry
        requestBody.put("temperature", 0.7);

        // Nagłówki HTTP z API key i typem danych
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        // Tworzymy obiekt żądania HTTP
        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

        try {
            // Wysłanie POST do OpenAI API
            String apiResponse = restTemplate.postForObject(openAiApiUrl, httpRequest, String.class);
            // Parsujemy odpowiedź JSON (która zawiera wynik w polu "content")
            JsonNode root = objectMapper.readTree(apiResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            // Parsujemy tekst content jako JSON (listę pytań)
            List<Question> questions = Arrays.asList(objectMapper.readValue(content, Question[].class));
            return questions;
        } catch (Exception e) {
            e.printStackTrace();
            // W razie błędu (np. problem z API) możemy zwrócić listę domyślnych pytań:
            return getFallbackQuestions();
        }
    }

    /**
     * Wysyła odpowiedzi użytkownika do GPT-4 i uzyskuje rekomendację filmu/serialu.
     * @param answers lista odpowiedzi użytkownika (w kolejności odpowiadającej pytaniom)
     * @return tekst rekomendacji wygenerowany przez GPT-4
     */
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

        // Budowa żądania podobnie jak w generateQuestions
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiModel);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

        try {
            String apiResponse = restTemplate.postForObject(openAiApiUrl, httpRequest, String.class);
            JsonNode root = objectMapper.readTree(apiResponse);
            String recommendationText = root.path("choices").get(0).path("message").path("content").asText();
            return recommendationText.trim();
        } catch (Exception e) {
            e.printStackTrace();
            // W razie błędu zwracamy komunikat o braku rekomendacji
            return "Niestety nie udało się uzyskać rekomendacji. Sprawdź logi serwera.";
        }
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

