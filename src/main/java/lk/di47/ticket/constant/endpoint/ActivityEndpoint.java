package lk.di47.ticket.constant.endpoint;

import lk.di47.ticket.constant.ApiConstant;

public final class ActivityEndpoint {
    private ActivityEndpoint() {
    }

    private static final String BASE = ApiConstant.API_V1 + "/activity-logs";

    public static final String LIST = BASE + "/list";
    public static final String DETAIL = BASE + "/detail";
}
