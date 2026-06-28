package lk.di47.ticket.constant.endpoint;

import lk.di47.ticket.constant.ApiConstant;

public final class AuthEndpoint {
    private AuthEndpoint() {
    }

    public static final String LOGIN = ApiConstant.API_V1 + "/auth/login";
    public static final String REGISTER = ApiConstant.API_V1 + "/auth/register";
    public static final String REFRESH_TOKEN = ApiConstant.API_V1 + "/auth/refresh-token";
    public static final String LOGOUT = ApiConstant.API_V1 + "/auth/logout";
}
