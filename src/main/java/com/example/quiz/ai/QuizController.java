package com.example.quiz.ai;


        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.web.bind.annotation.*;
        import java.util.List;
        import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin // umożliwia ewentualne żądania z innej domeny (tutaj niekonieczne, bo frontend jest serwowany lokalnie)
public class QuizController {

    @Autowired
    private OpenAiService openAiService;

    // Endpoint GET zwracający listę pytań quizu
    @GetMapping("/questions")
    public List<Question> getQuizQuestions() {
        // Możemy generować pytania dynamicznie przez OpenAI lub zwrócić stałą listę
        // Tutaj używamy OpenAI do wygenerowania pytań za każdym wywołaniem:
        List<Question> questions = openAiService.generateQuestions();
        return questions;
    }

    // Endpoint POST przyjmujący odpowiedzi i zwracający rekomendację
    @PostMapping("/recommendation")
    public Map<String, String> getRecommendation(@RequestBody AnswerRequest answerRequest) {
        // Pobranie listy odpowiedzi z obiektu AnswerRequest
        List<String> answers = answerRequest.getAnswers();
        // Wywołanie usługi OpenAI celem uzyskania rekomendacji na podstawie odpowiedzi
        String recommendation = openAiService.getRecommendation(answers);
        // Zwracamy odpowiedź jako JSON { "recommendation": "tekst rekomendacji" }
        return Map.of("recommendation", recommendation);
    }
}
