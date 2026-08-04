package com.santy.finances.controllers;

import com.santy.finances.DTOs.AuthResponse;
import com.santy.finances.DTOs.LoginRequest;
import com.santy.finances.DTOs.RegisterRequest;
import com.santy.finances.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST Request: Allows a user to register by providing necessary information.
     *
     * @param request Info needed to register.
     * @return HTTP 201 CREATED if successfully registered or
     *          400 BAD REQUEST if the email is already in use.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register (@RequestBody RegisterRequest request) {
        try {
            authService.register(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User successfully registered");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Catch errors like "Email registered" thrown by the service
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * POST Request: Allows a user to login by giving its username and password.
     *
     * @param request Info needed to log in.
     * @return HTTP 200 OK if correctly log in or
     *          401 UNAUTHORIZED if password or username do not exist.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Si falla la contraseña o el usuario no existe, devolvemos un 401 Unauthorized
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
