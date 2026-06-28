package lk.di47.ticket.constant.endpoint;

import lk.di47.ticket.constant.ApiConstant;

public final class DashboardEndpoint {
    private DashboardEndpoint() {
    }

    public static final String BASE = ApiConstant.API_V1 + "/dashboard";
    public static final String SUMMARY = BASE + "/summary";
}
