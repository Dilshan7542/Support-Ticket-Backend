package lk.di47.ticket.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.di47.ticket.constant.SecurityConstant;
import lk.di47.ticket.constant.SecurityPathConstant;
import lk.di47.ticket.entity.User;
import lk.di47.ticket.exception.BusinessException;
import lk.di47.ticket.exception.ErrorCode;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lk.di47.ticket.repository.UserRepository;
import lk.di47.ticket.response.ApiErrorResponseWriter;
import lk.di47.ticket.util.enums.Status;
import lk.di47.ticket.util.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final KeyExchangeService keyExchangeService;
    private final ApiErrorResponseWriter apiErrorResponseWriter;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(SecurityPathConstant.PUBLIC_PATHS).anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            ValidatedUserContext context = validateRequest(request);
            request.setAttribute(SecurityConstant.CURRENT_USER_ID, context.userId());
            request.setAttribute(SecurityConstant.CURRENT_SESSION_ID, context.sessionId());
            request.setAttribute(SecurityConstant.CURRENT_ENCRYPTION_KEY_ID, context.encryptionKeyId());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    context.username(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + context.role()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            apiErrorResponseWriter.write(response, exception);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private ValidatedUserContext validateRequest(HttpServletRequest request) {
        String authorization = Optional.ofNullable(request.getHeader(SecurityConstant.AUTHORIZATION_HEADER)).orElse("");
        if (!authorization.startsWith(SecurityConstant.BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing access token");
        }

        String encryptionKeyId = request.getHeader(SecurityConstant.KEY_ID_HEADER);
        if (encryptionKeyId == null || encryptionKeyId.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Missing encryption key id");
        }

        JwtTokenData tokenData = jwtService.validate(
                authorization.substring(SecurityConstant.BEARER_PREFIX.length()),
                TokenType.ACCESS
        );
        User user = userRepository.findById(tokenData.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid token user"));

        if (user.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Inactive user");
        }
        if (!user.getUsername().equals(tokenData.username()) || !user.getRole().name().equals(tokenData.role())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token user mismatch");
        }
        if (user.getActiveSessionId() == null || !user.getActiveSessionId().equals(tokenData.sessionId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Session is no longer active");
        }

        keyExchangeService.validateSession(encryptionKeyId);
        return new ValidatedUserContext(user.getId(), user.getUsername(), user.getRole().name(), tokenData.sessionId(), encryptionKeyId);
    }
}
