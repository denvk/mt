package com.denvk.mt.response;
/**
 * @author Denis Voroshchuk
 */
public class AccountException extends RuntimeException {
    
    private final StatusCode code;
    
    public AccountException(StatusCode code,String message) {
        super(message);
        this.code = code;
    }

    public StatusCode getCode() {
        return code;
    }
    
}
