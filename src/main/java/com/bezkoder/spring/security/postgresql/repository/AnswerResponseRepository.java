package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.AnswerResponse;
import com.bezkoder.spring.security.postgresql.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerResponseRepository extends JpaRepository<AnswerResponse,Long> {
    List<AnswerResponse> findByUser(User user);
}
