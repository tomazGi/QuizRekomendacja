package com.example.quiz.ai;


        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Controller;
        import org.springframework.web.bind.annotation.*;
        import java.util.List;
        import java.util.Map;

@Controller
@CrossOrigin // umożliwia ewentualne żądania z innej domeny (tutaj niekonieczne, bo frontend jest serwowany lokalnie)
public class QuizController {

    @Autowired
    private OpenAiService openAiService;

    // Endpoint GET zwracający listę pytań quizu
    @GetMapping("/questions")
    public List<Question> getQuizQuestions() {

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
