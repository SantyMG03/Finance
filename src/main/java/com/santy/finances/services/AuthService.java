package com.santy.finances.services;

import com.santy.finances.DTOs.AuthResponse;
import com.santy.finances.DTOs.LoginRequest;
import com.santy.finances.DTOs.RegisterRequest;
import com.santy.finances.exceptions.InvalidCredentialsException;
import com.santy.finances.models.User;
import com.santy.finances.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Transactional
    public User register(RegisterRequest request) {
        // Check if the email is already used
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Check if the username is already used
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already in use");
        }

        // Creates a new user and encrypts the password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        // Saves in database
        return userRepository.save(user);
    }

    public AuthResponse login (LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
