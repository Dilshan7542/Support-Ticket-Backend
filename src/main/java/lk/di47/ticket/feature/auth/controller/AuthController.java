package lk.di47.ticket.feature.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.constant.endpoint.AuthEndpoint;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.feature.auth.dto.*;
import lk.di47.ticket.feature.auth.service.AuthService;
import lk.di47.ticket.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(AuthEndpoint.LOGIN)
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            @RequestHeader(SecurityConstant.KEY_ID_HEADER) String encryptionKeyId) {
        return ApiResponse.success(MessageConstant.SUCCESS, authService.login(request, encryptionKeyId));
    }

    @PostMapping(AuthEndpoint.REGISTER)
    public ApiResponse<Long> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(MessageConstant.CREATED, authService.register(request));
    }

    @PostMapping(AuthEndpoint.REFRESH_TOKEN)
    public ApiResponse<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                           @RequestHeader(SecurityConstant.KEY_ID_HEADER) String encryptionKeyId) {
        return ApiResponse.success(MessageConstant.SUCCESS, authService.refreshToken(request, encryptionKeyId));
    }

    @PostMapping(AuthEndpoint.LOGOUT)
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request,
                                    HttpServletRequest servletRequest,
                                    @RequestHeader(SecurityConstant.KEY_ID_HEADER) String encryptionKeyId) {
        Long currentUserId = (Long) servletRequest.getAttribute(SecurityConstant.CURRENT_USER_ID);
        if (!request.userId().equals(currentUserId)) {
            throw new BusinessException(
                    lk.di47.ticket.exception.ErrorCode.UNAUTHORIZED,
                    "Logout user mismatch"
            );
        }
        authService.logout(currentUserId, encryptionKeyId);
        return ApiResponse.success(MessageConstant.SUCCESS, null);
    }
}
