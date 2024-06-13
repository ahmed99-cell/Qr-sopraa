package com.bezkoder.spring.security.postgresql.Dto;

import java.util.List;

public class UserActivity {
    private List<QuestionDto> questions;
    private List<AnswerDto> answers;
    private List<AnswerResponseDto> answerResponses;

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }

    public List<AnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerDto> answers) {
        this.answers = answers;
    }

    public List<AnswerResponseDto> getAnswerResponses() {
        return answerResponses;
    }

    public void setAnswerResponses(List<AnswerResponseDto> answerResponses) {
        this.answerResponses = answerResponses;
    }
}
