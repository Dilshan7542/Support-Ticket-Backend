package lk.di47.ticket.constant.endpoint;

import lk.di47.ticket.constant.ApiConstant;

public final class TicketEndpoint {
    private TicketEndpoint() {
    }

    private static final String BASE = ApiConstant.API_V1 + "/tickets";

    public static final String CREATE = BASE + "/create";
    public static final String LIST = BASE + "/list";
    public static final String DETAIL = BASE + "/detail";
    public static final String UPDATE_STATUS = BASE + "/update-status";
    public static final String ADD_REPLY = BASE + "/add-reply";
    public static final String ASSIGN = BASE + "/assign";
    public static final String UPLOAD_ATTACHMENT = BASE + "/upload-attachment";
}
