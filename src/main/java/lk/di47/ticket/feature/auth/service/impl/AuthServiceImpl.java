package lk.di47.ticket.feature.auth.service.impl;

import lk.di47.ticket.entity.User;
import lk.di47.ticket.entity.Vendor;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.auth.dto.*;
import lk.di47.ticket.feature.auth.service.AuthService;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.repository.VendorRepository;
import lk.di47.ticket.security.JwtService;
import lk.di47.ticket.security.JwtTokenData;
import lk.di47.ticket.util.HashUtil;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KeyExchangeService keyExchangeService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String encryptionKeyId) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid username or password");
        }
        if (user.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is inactive");
        }

        keyExchangeService.validateSession(encryptionKeyId);
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(user, sessionId);
        String refreshToken = jwtService.generateRefreshToken(user, sessionId);

        user.setActiveSessionId(sessionId);
        user.setRefreshTokenHash(HashUtil.sha256(refreshToken));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                accessToken,
                refreshToken,
                encryptionKeyId
        );
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, String encryptionKeyId) {
        JwtTokenData tokenData = jwtService.validate(request.refreshToken(), TokenType.REFRESH);
        if (!request.userId().equals(tokenData.userId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token user mismatch");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid user"));
        if (user.getStatus() != Status.ACTIVE || !user.getUsername().equals(tokenData.username())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid user session");
        }
        if (user.getActiveSessionId() == null || !user.getActiveSessionId().equals(tokenData.sessionId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token session expired");
        }
        if (user.getRefreshTokenHash() == null || !user.getRefreshTokenHash().equals(HashUtil.sha256(request.refreshToken()))) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid refresh token");
        }

        keyExchangeService.validateSession(encryptionKeyId);
        String newAccessToken = jwtService.generateAccessToken(user, tokenData.sessionId());
        String newRefreshToken = jwtService.generateRefreshToken(user, tokenData.sessionId());
        user.setRefreshTokenHash(HashUtil.sha256(newRefreshToken));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new RefreshTokenResponse(user.getId(), newAccessToken, newRefreshToken, encryptionKeyId);
    }

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(lk.di47.ticket.util.enums.UserRole.CUSTOMER);
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user).getId();
    }

    @Override
    @Transactional
    public Long createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Username already exists");
        }
        validateVendor(request.vendorId());

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setVendorId(request.vendorId());
        user.setRole(request.role());
        user.setStatus(Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user).getId();
    }

    @Override
    @Transactional
    public void logout(Long userId, String encryptionKeyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid user"));
        keyExchangeService.validateSession(encryptionKeyId);
        user.setActiveSessionId(null);
        user.setRefreshTokenHash(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void validateVendor(Long vendorId) {
        if (vendorId == null) {
            return;
        }
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor not found"));
        if (vendor.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Vendor is not active");
        }
    }
}
