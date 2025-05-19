
package com.example.quiz.ai;

import java.util.List;

public class Question {
    private String question;
    private List<String> options;

    // Konstruktor
    public Question(String question, List<String> options) {
        this.question = question;
        this.options = options;
    }
    // Pusty konstruktor potrzebny dla deserializacji (Jackson)
    public Question() {}

    // Gettery i Settery
    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }
    public List<String> getOptions() {
        return options;
    }
    public void setOptions(List<String> options) {
        this.options = options;
    }
}
