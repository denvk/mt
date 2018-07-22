package com.denvk.mt.response;

import static com.denvk.mt.response.StatusCode.*;
import com.denvk.mt.service.Account;
import com.denvk.mt.service.TransferDetails;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Denis Voroshchuk
 */
public class Response {

    @JsonProperty
    private final StatusCode code;
    @JsonProperty
    private final Object details;
    
    private Response(StatusCode code, Object details) {
        this.code = code;
        this.details = details;
    }

    public Response(Account account) {
        this.code = OK;
        this.details = account;
    }

    public Response(TransferDetails details) {
        this.code = OK;
        this.details = details;
    }

    public Response(AccountException e) {
        this.code = e.getCode();
        this.details = e.getMessage();
    }
    
    public static final Response INTERNAL_SERVER_ERROR_RESPONSE = new Response(INTERNAL_SERVER_ERROR,
            "An internal server error occurs. See logs for details");

}
