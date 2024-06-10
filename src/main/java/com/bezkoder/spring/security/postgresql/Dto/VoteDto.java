package com.bezkoder.spring.security.postgresql.Dto;

public class VoteDto {
    private Long id;
    private int value;
    private Long entityId;
    private String entityType;
    private String username;
    private Long questionId;
    private Long answerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getAnswerResponseId() {
        return answerResponseId;
    }

    public void setAnswerResponseId(Long answerResponseId) {
        this.answerResponseId = answerResponseId;
    }

    private Long answerResponseId;
}
