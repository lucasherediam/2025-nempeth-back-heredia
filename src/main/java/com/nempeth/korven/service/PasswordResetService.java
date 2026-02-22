package com.nempeth.korven.service;

import com.nempeth.korven.config.AppProperties;
import com.nempeth.korven.persistence.entity.PasswordResetToken;
import com.nempeth.korven.persistence.entity.User;
import com.nempeth.korven.persistence.repository.PasswordResetTokenRepository;
import com.nempeth.korven.persistence.repository.UserRepository;
import com.nempeth.korven.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final AppProperties appProps;

    private static final SecureRandom RNG = new SecureRandom();

    @Transactional
    public void startReset(String email, HttpServletRequest request) {
        Optional<User> optUser = userRepository.findByEmailIgnoreCase(email);
        if (optUser.isEmpty()) {
            // Response 200, so we don't reveal if email exists or not
            return;
        }
        User user = optUser.get();

        // Invalidate any previous reset tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        // Token generation
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        PasswordResetToken prt = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(token)
                .expiresAt(OffsetDateTime.now().plusMinutes(appProps.getResetTokenTtlMinutes()))
                .createdAt(OffsetDateTime.now())
                .build();

        tokenRepository.save(prt);

        // Frontend link: /reset-password?token=XYZ
        String resetLink = appProps.getFrontendBaseUrl().replaceAll("/+$","")
                + "/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isExpired() && !t.isUsed())
                .isPresent();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (prt.isExpired() || prt.isUsed()) {
            throw new IllegalArgumentException("Token expirado o ya utilizado");
        }

        User user = prt.getUser();
        user.setPasswordHash(PasswordUtils.hash(newPassword));
        prt.setUsedAt(OffsetDateTime.now());
    }
}
