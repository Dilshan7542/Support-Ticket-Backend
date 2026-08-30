package lk.di47.ticket.constant.endpoint;

import lk.di47.ticket.constant.ApiConstant;

public final class DepartmentEndpoint {
    private DepartmentEndpoint() {
    }

    private static final String BASE = ApiConstant.API_V1 + "/departments";

    public static final String CREATE = BASE + "/create";
    public static final String LIST = BASE + "/list";
    public static final String DETAIL = BASE + "/detail";
    public static final String UPDATE = BASE + "/update";
    public static final String USERS = BASE + "/users";
}
