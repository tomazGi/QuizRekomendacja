
package com.example.quiz.ai;

import lombok.Data;

import java.util.List;

@Data
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


}
