package com.example.wallet.controller;

import com.example.wallet.model.RefreshToken;
import com.example.wallet.model.User;
import com.example.wallet.repository.RefreshTokenRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.security.CustomUserDetails;
import com.example.wallet.security.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of("USER"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = createAndSaveRefreshToken(userDetails.getUser());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String requestRefreshToken = body.get("refreshToken");
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(requestRefreshToken);

        if (tokenOpt.isPresent()) {
            RefreshToken refreshToken = tokenOpt.get();

            if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
                refreshTokenRepository.delete(refreshToken);
                return ResponseEntity.status(403).body("Refresh token expired");
            }

            String newAccessToken = jwtTokenProvider.generateAccessToken(
                    new CustomUserDetails(refreshToken.getUser())
            );

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }

        return ResponseEntity.status(403).body("Invalid refresh token");
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String requestRefreshToken = body.get("refreshToken");
        refreshTokenRepository.findByToken(requestRefreshToken).ifPresent(refreshTokenRepository::delete);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private String createAndSaveRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiration());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
