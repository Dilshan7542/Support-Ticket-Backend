package lk.di47.ticket.feature.security.controller;

import jakarta.validation.Valid;
import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.SecurityEndpoint;
import lk.di47.ticket.feature.security.dto.KeyExchangeRequest;
import lk.di47.ticket.feature.security.dto.KeyExchangeResponse;
import lk.di47.ticket.feature.security.service.KeyExchangeService;
import lk.di47.ticket.response.ApiResponse;
import lk.di47.ticket.util.mask.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequiredArgsConstructor
@Log4j2
public class KeyExchangeController {
    private final KeyExchangeService keyExchangeService;
    private final JsonMapper jsonMapper;

    /**
     * This is the only API that is intentionally not AES encrypted. It establishes the key used by all other APIs.
     * The key exchange session belongs to the running client application session, independent of login state.
     */
    @PostMapping(SecurityEndpoint.KEY_EXCHANGE)
    public ApiResponse<KeyExchangeResponse> exchange(@Valid @RequestBody KeyExchangeRequest request) {
        log.debug("Key Exchange -> {}", this.toJson(request));
        KeyExchangeResponse response = keyExchangeService.createExchange(request);
        return ApiResponse.success(MessageConstant.SUCCESS, response);
    }

    private String toJson(Object data) {
        return SensitiveDataMasker.mask(jsonMapper.writeValueAsString(data));
    }
}
