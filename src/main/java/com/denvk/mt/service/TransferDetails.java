package com.denvk.mt.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * @author Denis Voroshchuk
 */
public class TransferDetails {

    @JsonProperty
    private final Account source;
    @JsonProperty
    private final Account target;
    @JsonProperty
    private final BigDecimal transfered;

    public TransferDetails(Account source, Account target, BigDecimal transfered) {
        this.source = source;
        this.target = target;
        this.transfered = transfered;
    }

    public Account getSource() {
        return source;
    }

    public Account getTarget() {
        return target;
    }

    public BigDecimal getTransfered() {
        return transfered;
    }
    
    
}
