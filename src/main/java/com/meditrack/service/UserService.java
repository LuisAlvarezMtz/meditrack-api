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

    public Map<String, String> acceder(User user) {
        Authentication authentication =
                authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                user.getPhoneNumber(),
                                user.getPassword()
                        )
                );

        if (authentication.isAuthenticated()) {
            User userBD = userRepository.findByPhoneNumber(user.getPhoneNumber())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            String accessToken = jwtService.generateToken(userBD);
            String refreshToken = jwtService.generateRefreshToken(userBD);
            return Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            );
        }
        throw new BadRequestException("Credenciales incorrectas");
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            String telefono = jwtService.extractPhoneNumber(refreshToken);
            User user = userRepository.findByPhoneNumber(telefono)
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

            if (!jwtService.validateToken(refreshToken)) {
                throw new BadRequestException("Refresh token inválido o expirado");
            }

            return jwtService.generateToken(user);

        }catch (BadRequestException | NotFoundException e) {
        throw e;
        } catch (Exception e) {
        throw new BadRequestException("Error al refrescar token");
        }
    }
}