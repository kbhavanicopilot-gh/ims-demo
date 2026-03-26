package com.htc.incidentmanagement.controller;

import com.htc.incidentmanagement.dto.*;
import com.htc.incidentmanagement.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.htc.incidentmanagement.service.AuthTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login & Logout module.")
public class AuthController {

    private final AuthService authService;
    private final AuthTokenService authTokenService;

    public AuthController(AuthService authService, AuthTokenService authTokenService) {
        this.authService = authService;
        this.authTokenService = authTokenService;
    }

    // -------------------- LOGIN --------------------
    @Operation(summary = "Employee login", description = "Authenticates an employee using email and password and returns a JWT access token. Only active employees are allowed to log in.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // -------------------- SET / UPDATE PASSWORD --------------------
    @Operation(summary = "Set or update password for employee", description = "Allows an existing employee to set or update their password. Intended for first-time password setup or admin-initiated resets.")
    @PutMapping("/employees/{employeeId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long employeeId,
            @RequestBody UpdatePasswordRequest request) {

        authService.setOrUpdatePassword(employeeId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // -------------------- FORGOT PASSWORD --------------------
    @Operation(summary = "Forgot password", description = "Allows an employee to reset their password after identity verification. Existing tokens are invalidated after reset.")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.initiateForgotPassword(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    // ================== Auth Token methods==================

    private static final String BEARER_PREFIX = "Bearer ";

    // -------------------- VALIDATE TOKEN --------------------
    @Operation(summary = "Validate JWT token", description = "Validates the JWT token supplied in the Authorization header.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractBearerToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Authorization header missing or invalid format. Expected: Bearer <token>");
        }
        return ResponseEntity.ok(authTokenService.isTokenValid(token));
    }

    // -------------------- LOGOUT --------------------
    @Operation(summary = "Logout and revoke token", description = "Revokes the current JWT token and prevents further access using the same token.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractBearerToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Authorization header missing or invalid format. Expected: Bearer <token>");
        }
        authTokenService.revokeToken(token);
        return ResponseEntity.noContent().build();
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // -------------------- FORCE LOGOUT USER --------------------
    @Operation(summary = "Revoke all tokens for a user (Admin)", description = "Revokes the current JWT token and prevents further access using the same token.")
    @PostMapping("/revoke/user/{userId}")
    public ResponseEntity<Void> revokeAllTokens(
            @PathVariable Long userId) {

        authTokenService.revokeAllTokensForUser(userId);
        return ResponseEntity.noContent().build();
    }
}
