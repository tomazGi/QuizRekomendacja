package com.example.quiz.ai;


import java.util.List;

public class AnswerRequest {
    private List<String> answers;

    public AnswerRequest() {}

    public AnswerRequest(List<String> answers) {
        this.answers = answers;
    }

    public List<String> getAnswers() {
        return answers;
    }
    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}

