package com.example.roadworks.service;

import com.example.roadworks.model.User;
import com.example.roadworks.model.enums.UserRole;
import com.example.roadworks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User createUser(String username, String rawPassword, String fullName, String email, UserRole role) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .email(email)
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    public User updateUser(Long id, String fullName, String email, UserRole role, boolean active) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User with id=" + id + " does not exist."));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
        return userRepository.save(user);
    }

    public void changePassword(Long id, String newRawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id=" + id + " does not exist."));
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
