package com.santy.finances.services;

import com.santy.finances.DTOs.AuthResponse;
import com.santy.finances.DTOs.LoginRequest;
import com.santy.finances.DTOs.RegisterRequest;
import com.santy.finances.exceptions.InvalidCredentialsException;
import com.santy.finances.models.User;
import com.santy.finances.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("santy");
        request.setEmail("santy@test.com");
        request.setPassword("secret");
        return request;
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("santy");
        user.setEmail("santy@test.com");
        user.setPassword("encodedPassword");
        return user;
    }

    @Test
    void register_success_savesUserWithEncodedPasswordAndUserRole() {
        RegisterRequest request = buildRegisterRequest();
        User savedUser = buildUser();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(request);

        assertThat(result).isSameAs(savedUser);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();
        assertThat(toSave.getUsername()).isEqualTo("santy");
        assertThat(toSave.getEmail()).isEqualTo("santy@test.com");
        assertThat(toSave.getPassword()).isEqualTo("encodedPassword");
        assertThat(toSave.getRole()).isEqualTo("USER");
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = buildRegisterRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(buildUser()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenUsernameAlreadyExists() {
        RegisterRequest request = buildRegisterRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(buildUser()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username is already in use");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("santy");
        request.setPassword("secret");
        User user = buildUser();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_throwsWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("santy");
        request.setPassword("secret");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("User not found");
    }

    @Test
    void login_throwsWhenPasswordIncorrect() {
        LoginRequest request = new LoginRequest();
        request.setUsername("santy");
        request.setPassword("wrong");
        User user = buildUser();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Incorrect password");
    }
}
