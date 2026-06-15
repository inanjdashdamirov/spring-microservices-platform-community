package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.domain.UserStatus;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.exception.ApiException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponseDTO getCurrentUser(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User profile not found"));

        return UserMapper.toDTO(user);
    }

    public UserResponseDTO getUserById(Long id, String callerEmail, boolean isAdmin) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!isAdmin && !user.getEmail().equals(callerEmail)) {
            throw ApiException.of(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        return UserMapper.toDTO(user);
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable).map(UserMapper::toDTO);
    }

    public UserResponseDTO updateUserStatus(Long id, UserStatus status) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        log.info("Updating user status id={}, status={}", id, status);

        user.setStatus(status);
        User saved = repository.save(user);

        return UserMapper.toDTO(saved);
    }
}
