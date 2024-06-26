package com.bezkoder.spring.security.postgresql.service;

import com.bezkoder.spring.security.postgresql.Dto.*;
import com.bezkoder.spring.security.postgresql.Exeception.ResourceNotFoundException;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.payload.request.AnswerRequest;
import com.bezkoder.spring.security.postgresql.payload.request.QuestionRequest;
import com.bezkoder.spring.security.postgresql.payload.response.MessageResponse;
import com.bezkoder.spring.security.postgresql.repository.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImp implements QuestionService{
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnswerResponseRepository answerResponseRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    QuestionRepositoryCustom questionRepositoryCustom;
    @Autowired
private UserServiceImp userServiceImp;


    @Override
    public List<QuestionDto> getAllQuestions(QuestionSearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPageIndex(), searchRequest.getPageSize());
        List<Question> questions = questionRepositoryCustom.findByCriteria(
                searchRequest.getTitle(),
                searchRequest.getContent(),
                searchRequest.getUserId(),
                searchRequest.getTags(),
                pageable
        );

        return questions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public QuestionDto mapToDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        Boolean isUserAnonymous = question.getUserAnonymous();
        if (isUserAnonymous == null || !isUserAnonymous) {
            dto.setUsername(question.getUser().getUsername());
        } else {
            dto.setUsername(null);
        }
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        dto.setTags(question.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));

        return dto;
    }
    @Override
    public Question createQuestion(QuestionRequest questionRequest, String username, MultipartFile file, Long tagId, Boolean isUserAnonymous) {
        if (questionRequest == null) {
            throw new IllegalArgumentException("QuestionRequest cannot be null");
        }

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new RuntimeException("Tag not found"));

            Question question = new Question();
            question.setTitle(questionRequest.getTitle() );
            question.setContent(questionRequest.getContent() );
            question.setUser(user);
            question.setCreatedAt(new Date());
            question.getTags().add(tag);
            question.setUserAnonymous(isUserAnonymous);

            if (file != null) {

                String contentType = file.getContentType();

                if (!contentType.equals("image/jpeg") && !contentType.equals("application/pdf") && !contentType.equals("text/csv")) {
                    throw new RuntimeException("Unsupported file type");
                }

                try {
                    question.setFile(file.getBytes());
                    question.setContentType(file.getContentType());
                } catch (IOException e) {
                    throw new RuntimeException("Error reading file", e);
                }
            }
        userServiceImp.increaseReputation(user.getMatricule());

            questionRepository.save(question);

        return null;
    }
    public void associateTagWithQuestion(Long questionId, Long tagId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", tagId));

        question.getTags().add(tag);
        questionRepository.save(question);
    }
    private GetQuestionByIdDto maptoDto(Question question) {
        GetQuestionByIdDto dto = new GetQuestionByIdDto();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setUsername(question.getUser().getUsername());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
dto.setTags(question.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
        dto.setFile(question.getFile());
        dto.setContentType(question.getContentType());
        dto.setTags(question.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));

        dto.setAnswers(question.getAnswers().stream().map(this::mapAnswerToDto).collect(Collectors.toList()));
        dto.setVotes(question.getVotes().stream().map(this::mapVoteToDto).collect(Collectors.toList()));
        dto.setFavorites(question.getFavorites().stream().map(this::mapFavoriteToDto).collect(Collectors.toList()));
        return dto;


    }
    @Override
    public AnswerDto mapAnswerToDto(Answer answer) {
        AnswerDto dto = new AnswerDto();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setUsername(answer.getUser().getUsername());
        dto.setCreatedAt(answer.getCreatedAt().toString());
        dto.setUpdatedAt(answer.getUpdatedAt() != null ? answer.getUpdatedAt().toString() : null);
        dto.setResponses(answer.getResponses().stream().map(AnswerResponse::getContent).collect(Collectors.toList()));

        dto.setVotes(answer.getVotes().stream().map(Vote::toString).collect(Collectors.toList()));
        dto.setFavorites(answer.getFavorites().stream().map(Favorite::toString).collect(Collectors.toList()));
        return dto;
    }
    @Override
    public AnswerResponseDto mapToAnswerResponseDto(AnswerResponse answerResponse) {
        AnswerResponseDto dto = new AnswerResponseDto();
        dto.setId(answerResponse.getId());
        dto.setContent(answerResponse.getContent());
        dto.setUsername(answerResponse.getUser().getUsername());
        dto.setCreatedAt(answerResponse.getCreatedAt().toString());
        dto.setUpdatedAt(answerResponse.getUpdatedAt() != null ? answerResponse.getUpdatedAt().toString() : null);
        dto.setVotes(answerResponse.getVotes().stream().map(Vote::toString).collect(Collectors.toList()));
        dto.setFavorites(answerResponse.getFavorites().stream().map(Favorite::toString).collect(Collectors.toList()));
        return dto;
    }

    private VoteDto mapVoteToDto(Vote vote) {
        VoteDto dto = new VoteDto();
        dto.setId(vote.getId());
        dto.setUsername(vote.getUser().getUsername());
        dto.setValue(vote.getValue());
        return dto;
    }

    private FavoriteDto mapFavoriteToDto(Favorite favorite) {
        FavoriteDto dto = new FavoriteDto();
        dto.setId(favorite.getId());
        dto.setUsername(favorite.getUser().getUsername());
        return dto;
    }

    @Override
    public Optional<GetQuestionByIdDto> getQuestionById(Long id) {
        return questionRepository.findById(id).map(this::maptoDto);
    }


    @Override
    public Question updateQuestion(Long questionId, QuestionRequest questionRequest) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setTitle(questionRequest.getTitle());
        question.setContent(questionRequest.getContent());
        question.setUpdatedAt(new Date());
        return questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        questionRepository.delete(question);
    }

    @Override
    public Answer getAnswerById(Long questionId, Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Answer does not belong to the specified question");
        }
        return answer;
    }

    @Override
    public Answer updateAnswer(Long questionId, Long answerId, AnswerRequest answerRequest) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Answer does not belong to the specified question");
        }
        answer.setContent(answerRequest.getContent());
        answer.setUpdatedAt(new Date());
        return answerRepository.save(answer);
    }

    @Override
    public void deleteAnswer(Long questionId, Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Answer does not belong to the specified question");
        }
        answerRepository.delete(answer);
    }

    @Override
    public List<AnswerDto> getAnswersByQuestionId(Long questionId) {
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        return answers.stream()
                .map(this::mapAnswerToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Answer createAnswer(Long questionId, AnswerRequest answerRequest, String username,MultipartFile file) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new RuntimeException("Question not found"));
        Hibernate.initialize(question.getUser());
        String questionCreatorEmail = question.getUser().getEmail();
        Answer answer = new Answer();
        answer.setContent(answerRequest.getContent());
        answer.setUser(user);
        answer.setQuestion(question);


        answer.setCreatedAt(new Date());
        if (file != null) {

            String contentType = file.getContentType();

            if (!contentType.equals("image/jpeg") && !contentType.equals("application/pdf") && !contentType.equals("text/csv")) {
                throw new RuntimeException("Unsupported file type");
            }

            try {
                answer.setFile(file.getBytes());
                answer.setContentType(file.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Error reading file", e);
            }
        }

        Answer savedAnswer = answerRepository.save(answer);
        userServiceImp.increaseReputation(user.getMatricule());


        Notification notification = new Notification();
        notification.setUser(question.getUser());
        notification.setContent("Une nouvelle réponse a été ajoutée à votre question");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        sendNotificationEmail(questionCreatorEmail, "Une nouvelle réponse a été ajoutée à votre question");


        return savedAnswer;
    }

    @Override
    public AnswerResponse createResponseToAnswer(Long questionId, Long parentAnswerId, AnswerRequest answerRequest, String username,MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        Answer parentAnswer = answerRepository.findById(parentAnswerId)
                .orElseThrow(() -> new RuntimeException("Parent Answer not found"));
        String questionCreatorEmail = parentAnswer.getUser().getEmail();
        Hibernate.initialize(parentAnswer.getUser());


        if (!parentAnswer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Parent Answer does not belong to the specified question");
        }

        AnswerResponse response = new AnswerResponse();
        response.setContent(answerRequest.getContent());
        response.setUser(user);
        response.setParentAnswer(parentAnswer);
        response.setCreatedAt(new Date());
        if (file != null) {

            String contentType = file.getContentType();

            if (!contentType.equals("image/jpeg") && !contentType.equals("application/pdf") && !contentType.equals("text/csv")) {
                throw new RuntimeException("Unsupported file type");
            }

            try {
                response.setFile(file.getBytes()  );
                response.setContentType(file.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Error reading file", e);
            }
        }


        AnswerResponse savedResponse = answerResponseRepository.save(response);
        userServiceImp.increaseReputation(user.getMatricule());


        Notification notification = new Notification();
        notification.setUser(parentAnswer.getUser());
        notification.setContent("Une nouvelle réponse à une réponse a été ajoutée");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        sendNotificationEmail(questionCreatorEmail, "Une nouvelle réponse a été ajoutée à votre réponse de question");


        return savedResponse;
    }
    @Transactional
    public void sendNotificationEmail(String userEmail, String content) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(userEmail);
        email.setSubject("Notification");
        email.setText(content);
        mailSender.send(email);
    }




    @Override
    public List<AnswerResponseDto> getResponsesToAnswer(Long questionId, Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Answer does not belong to the specified question");
        }

        Set<AnswerResponse> responses = answer.getResponses();
        return responses.stream()
                .map(this::mapToAnswerResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserActivity getUserActivity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails)principal).getUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Question> questions = new ArrayList<>(user.getQuestions());
            List<Answer> answers = new ArrayList<>(user.getAnswers());
            List<AnswerResponse> answerResponses = answerResponseRepository.findByUser(user); // Fetch AnswerResponse objects for the user

            List<QuestionDto> questionDtos = questions.stream().map(this::mapToDto).collect(Collectors.toList());
            List<AnswerDto> answerDtos = answers.stream().map(this::mapAnswerToDto).collect(Collectors.toList());
            List<AnswerResponseDto> answerResponseDtos = answerResponses.stream().map(this::mapToAnswerResponseDto).collect(Collectors.toList());

            UserActivity userActivity = new UserActivity();
            userActivity.setQuestions(questionDtos);
            userActivity.setAnswers(answerDtos);
            userActivity.setAnswerResponses(answerResponseDtos);

            return userActivity;
        }

        throw new RuntimeException("User not authenticated");
    }

    @Override
    public AnswerResponse updateResponseToAnswer(Long questionId, Long parentAnswerId, Long responseId, AnswerRequest answerRequest, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer parentAnswer = answerRepository.findById(parentAnswerId)
                .orElseThrow(() -> new RuntimeException("Parent Answer not found"));

        if (!parentAnswer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Parent Answer does not belong to the specified question");
        }

        AnswerResponse response = answerResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Response to Answer not found"));

        if (!response.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User is not authorized to update this response to answer");
        }

        response.setContent(answerRequest.getContent());

        return answerResponseRepository.save(response);
    }

    @Override
    public void deleteResponseToAnswer(Long questionId, Long parentAnswerId, Long responseId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer parentAnswer = answerRepository.findById(parentAnswerId)
                .orElseThrow(() -> new RuntimeException("Parent Answer not found"));

        if (!parentAnswer.getQuestion().getId().equals(questionId)) {
            throw new RuntimeException("Parent Answer does not belong to the specified question");
        }

        AnswerResponse response = answerResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Response to Answer not found"));

        if (!response.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User is not authorized to delete this response to answer");
        }

        answerResponseRepository.delete(response);
    }
    public void associateTagWithQuestion(Long questionId, Tag tag) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        question.getTags().add(tag);
        questionRepository.save(question);
    }

    public void dissociateTagFromQuestion(Long questionId, Long tagId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        question.getTags().removeIf(tag -> tag.getId().equals(tagId));
        questionRepository.save(question);
    }

    @Override
    public List<Question> getQuestionsWithAnswers() {
        return questionRepository.findAll().stream()
                .filter(question -> !question.getAnswers().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<Question> getQuestionsWithoutAnswers() {
        return questionRepository.findAll().stream()
                .filter(question -> question.getAnswers().isEmpty())
                .collect(Collectors.toList());
    }
}
