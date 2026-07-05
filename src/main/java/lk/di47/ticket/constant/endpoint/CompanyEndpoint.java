package lk.di47.ticket.constant.endpoint;

import lk.di47.ticket.constant.ApiConstant;

public final class CompanyEndpoint {
    private CompanyEndpoint() {
    }

    private static final String BASE = ApiConstant.API_V1 + "/companies";

    public static final String CREATE = BASE + "/create";
    public static final String LIST = BASE + "/list";
    public static final String DETAIL = BASE + "/detail";
    public static final String UPDATE = BASE + "/update";
}
