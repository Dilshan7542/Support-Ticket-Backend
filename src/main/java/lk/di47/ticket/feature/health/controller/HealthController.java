package lk.di47.ticket.feature.health.controller;

import lk.di47.ticket.constant.MessageConstant;
import lk.di47.ticket.constant.endpoint.HealthEndpoint;
import lk.di47.ticket.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @PostMapping(HealthEndpoint.STATUS)
    public ApiResponse<String> status() {
        return ApiResponse.success(MessageConstant.SUCCESS, "UP");
    }
}
