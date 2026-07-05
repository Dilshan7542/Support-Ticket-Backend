package lk.di47.ticket.feature.security.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.constant.endpoint.SecurityEndpoint;
import lk.di47.ticket.entity.User;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.security.dto.KeyExchangeRequest;
import lk.di47.ticket.feature.security.dto.KeyExchangeResponse;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.security.JwtService;
import lk.di47.ticket.security.JwtTokenData;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.TokenType;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequiredArgsConstructor
@Log4j2
public class KeyExchangeController {
    private final KeyExchangeService keyExchangeService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final JsonMapper jsonMapper;

    /**
     * This is the only API that is intentionally not AES encrypted. It establishes the key used by all other APIs.
     * When an existing access token is supplied during application reload, the new key exchange session is bound to
     * the authenticated user so that a page refresh does not require another login.
     */
    @PostMapping(SecurityEndpoint.KEY_EXCHANGE)
    public ApiResponse<KeyExchangeResponse> exchange(
            @Valid @RequestBody KeyExchangeRequest request,
            @RequestHeader(value = SecurityConstant.AUTHORIZATION_HEADER, required = false) String authorization
    ) {
        log.debug("Key Exchange -> {}", this.toJson(request));
        KeyExchangeResponse response = keyExchangeService.createExchange(request);
        bindToExistingUserWhenTokenIsPresent(response.keyId(), authorization);
        return ApiResponse.success(MessageConstant.SUCCESS, response);
    }

    private void bindToExistingUserWhenTokenIsPresent(String keyId, String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return;
        }
        if (!authorization.startsWith(SecurityConstant.BEARER_PREFIX)) {
            keyExchangeService.invalidate(keyId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid authorization header");
        }

        try {
            JwtTokenData tokenData = jwtService.validate(
                    authorization.substring(SecurityConstant.BEARER_PREFIX.length()),
                    TokenType.ACCESS
            );
            User user = userRepository.findById(tokenData.userId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token user"));

            if (user.getStatus() != Status.ACTIVE
                    || !user.getUsername().equals(tokenData.username())
                    || !user.getRole().name().equals(tokenData.role())
                    || user.getActiveSessionId() == null
                    || !user.getActiveSessionId().equals(tokenData.sessionId())) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "Session is no longer active");
            }

            keyExchangeService.bindToUser(keyId, user.getId());
        } catch (BusinessException exception) {
            keyExchangeService.invalidate(keyId);
            throw exception;
        }
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
