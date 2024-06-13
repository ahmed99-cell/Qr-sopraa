package com.bezkoder.spring.security.postgresql.service;

import com.bezkoder.spring.security.postgresql.Dto.*;
import com.bezkoder.spring.security.postgresql.models.Answer;
import com.bezkoder.spring.security.postgresql.models.AnswerResponse;
import com.bezkoder.spring.security.postgresql.models.Question;
import com.bezkoder.spring.security.postgresql.models.Tag;
import com.bezkoder.spring.security.postgresql.payload.request.AnswerRequest;
import com.bezkoder.spring.security.postgresql.payload.request.QuestionRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface QuestionService {
    List<QuestionDto> getAllQuestions(QuestionSearchRequestDto searchRequest);
     QuestionDto mapToDto(Question question);
    public Question createQuestion(QuestionRequest questionRequest, String username, MultipartFile file, Long tagId, Boolean isUserAnonymous) ;
     void associateTagWithQuestion(Long questionId, Long tagId);

    public Optional<GetQuestionByIdDto> getQuestionById(Long id);
    List<AnswerDto> getAnswersByQuestionId(Long questionId);
    List<AnswerResponseDto> getResponsesToAnswer(Long questionId, Long answerId);
    UserActivity getUserActivity();
    AnswerResponseDto mapToAnswerResponseDto(AnswerResponse answerResponse);


    AnswerDto mapAnswerToDto(Answer answer);


    Question updateQuestion(Long questionId, QuestionRequest questionRequest);
    void deleteQuestion(Long questionId);
    Answer getAnswerById(Long questionId, Long answerId);
    Answer updateAnswer(Long questionId, Long answerId, AnswerRequest answerRequest);
    void deleteAnswer(Long questionId, Long answerId);

    Answer createAnswer(Long questionId, AnswerRequest answerRequest, String username);
    AnswerResponse createResponseToAnswer(Long questionId, Long parentAnswerId, AnswerRequest answerRequest, String username);
    AnswerResponse updateResponseToAnswer(Long questionId, Long parentAnswerId, Long responseId, AnswerRequest answerRequest, String username);
    void deleteResponseToAnswer(Long questionId, Long parentAnswerId, Long responseId, String username);
     void associateTagWithQuestion(Long questionId, Tag tag);
    void dissociateTagFromQuestion(Long questionId, Long tagId);
    List<Question> getQuestionsWithAnswers();
    List<Question> getQuestionsWithoutAnswers();

}
