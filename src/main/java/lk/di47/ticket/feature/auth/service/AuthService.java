package lk.di47.ticket.feature.auth.service;

import lk.di47.ticket.feature.auth.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request, String encryptionKeyId);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request, String encryptionKeyId);

    Long register(RegisterRequest request);

    void logout(Long userId, String encryptionKeyId);
}
