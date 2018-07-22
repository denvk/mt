package com.denvk.mt.response;

import com.denvk.mt.service.Account;
import java.math.BigDecimal;

/**
 * @author Denis Voroshchuk
 */
public class NotEnoughFundsException extends AccountException {
    
    public NotEnoughFundsException(Account a,BigDecimal value) {
        super(StatusCode.NOT_ENOUGH_FUNDS,
                "There is not enough funds on account "+a.getId()+" to transfer "+value.toPlainString()+", actual amount is "+a.getAmount().toPlainString());
    }
    
}
