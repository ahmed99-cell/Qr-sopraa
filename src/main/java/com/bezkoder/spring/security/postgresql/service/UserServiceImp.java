package com.bezkoder.spring.security.postgresql.service;

import com.bezkoder.spring.security.postgresql.Dto.UserDto;
import com.bezkoder.spring.security.postgresql.Exeception.ResourceNotFoundException;
import com.bezkoder.spring.security.postgresql.Exeception.UserNotFoundException;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.repository.BadgeRepository;
import com.bezkoder.spring.security.postgresql.repository.ReputationRepository;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import com.bezkoder.spring.security.postgresql.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BadgeRepository badgeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private ReputationRepository reputationRepository;


    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

    }
    public UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setMatricul(user.getMatricule());
        userDto.setEmail(user.getEmail());
        userDto.setNom(user.getNom());
        userDto.setPrenom(user.getPrenom());
        userDto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return userDto;
    }

    @Override
    public User getUserById(Long matricule) {
        return userRepository.findById(matricule)
                .orElseThrow(() -> new UserNotFoundException(matricule));
    }

    @Override
    public void deleteUser(Long matricule) {
        if (!userRepository.existsById(matricule)) {
            throw new UserNotFoundException(matricule);
        }
        userRepository.deleteById(matricule);

    }

    @Override
    public User updateUser(User newUser, Long matricule) {
        return userRepository.findById(matricule).map(user -> {
            user.setUsername(newUser.getUsername());
            user.setNom(newUser.getNom());
            user.setPrenom(newUser.getPrenom());
            user.setEmail(newUser.getEmail());

            String encryptedPassword = encoder.encode(newUser.getPassword());
            user.setPassword(encryptedPassword);



            return userRepository.save(user);
        }).orElseThrow(() -> new UserNotFoundException(matricule));
    }
    @Transactional
    public void increaseReputation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        Reputation reputation = user.getReputation();
        if (reputation == null) {
            reputation = new Reputation();
            reputation.setUser(user);
            user.setReputation(reputation);
        }
        reputation.setScore(reputation.getScore() + 5);
        userRepository.save(user);
        checkAndUpdateBadge(user);
    }
    @Override
    public void updateReputationUserId(Long reputationId, Long userId) {
        Reputation reputation = reputationRepository.findById(reputationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reputation", "id", reputationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        reputation.setUser(user);

        reputationRepository.save(reputation);
    }

    private Badge createBadge(String name) {
        Badge badge = new Badge();
        badge.setName(name);
        return badgeRepository.save(badge);
    }

    @Override
    public void checkAndUpdateBadge(User user) {
        int score = user.getReputation().getScore();
        Badge badge;
        if (score >= 60) {
            badge = badgeRepository.findByName("Gold")
                    .orElseGet(() -> createBadge("Gold"));
        } else if (score >= 40) {
            badge = badgeRepository.findByName("Silver")
                    .orElseGet(() -> createBadge("Silver"));
        } else {
            badge = badgeRepository.findByName("Bronze")
                    .orElseGet(() -> createBadge("Bronze"));
        }
        user.getBadges().clear();
        user.getBadges().add(badge);
        userRepository.save(user);
    }

    @Override

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    @Override
    public void updateUserRole(Long userId, String newRoleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        Set<Role> updatedRoles = new HashSet<>();
        Role newRole = roleRepository.findByName(ERole.valueOf(newRoleName))
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        updatedRoles.add(newRole);

        user.setRoles(updatedRoles);
        userRepository.save(user);
    }

    }




