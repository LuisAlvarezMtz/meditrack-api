package com.meditrack.service;

import com.meditrack.exception.BadRequestException;
import com.meditrack.exception.NotFoundException;
import com.meditrack.model.User;
import com.meditrack.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class UserService {

    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    public UserService(AuthenticationManager authManager,
                       JWTService jwtService,
                       UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public Map<String, String> login(User user) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getPhoneNumber(),
                                user.getPassword()
                        )
                );

        if (authentication.isAuthenticated()) {
            User userBD = userRepository.findByPhoneNumber(user.getPhoneNumber())
                    .orElseThrow(() -> new NotFoundException("User not found"));
            String accessToken = jwtService.generateToken(userBD);
            String refreshToken = jwtService.generateRefreshToken(userBD);
            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        }
        throw new BadRequestException("Invalid credentials");
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            String phoneNumber = jwtService.extractPhoneNumber(refreshToken);
            User user = userRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (!jwtService.validateToken(refreshToken)) {
                throw new BadRequestException("Invalid or expired refresh token");
            }

            return jwtService.generateToken(user);

        }catch (BadRequestException | NotFoundException e) {
        throw e;
        } catch (Exception e) {
        throw new BadRequestException("Error refreshing token");
        }
    }
}