package com.denvk.mt.service;

import com.denvk.mt.response.AccountException;
import com.denvk.mt.response.NotEnoughFundsException;
import com.denvk.mt.response.StatusCode;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Denis Voroshchuk
 */
public class AccountService {

    private final InMemoryDatastore store;

    public AccountService(InMemoryDatastore store) {
        this.store = store;
    }

    public Account create(String id, BigDecimal init) {
        id = checkAndTrimId(id);
        checkInitValue(init);
        Account created = store.create(id, init);
        if (created != null) {
            //new account is created;
            return created;
        } else {
            //already exists;
            throw new AccountException(StatusCode.ALREADY_EXISTS,
                    String.format("The account '%s' already exists.",id));
        }
    }

    public Account get(String id) {
        id = checkAndTrimId(id);
        Account account = store.get(id);
        if (account == null) {
            throw new AccountException(StatusCode.NOT_FOUND,
                    String.format("The account '%s' is not found.",id));
        }
        return account;
    }

    public TransferDetails transfer(String sId, String tId, BigDecimal value) {
        sId = checkAndTrimId(sId);
        tId = checkAndTrimId(tId);
        checkSameId(sId, tId);
        checkTransferValue(value);

        Account sAccount = this.get(sId);
        Account tAccount = this.get(tId);
        //order accounts by internal ID to prevent classic deadlock.
        Account[] locks = Account.order(sAccount, tAccount);
        synchronized (locks[0]) {
            //check if enough money before lock second account
            if (sAccount.equals(locks[0])
                    && sAccount.getAmount().compareTo(value) == -1) {
                throw new NotEnoughFundsException(sAccount, value);
            }
            synchronized (locks[1]) {
                if (sAccount.equals(locks[1])
                        && sAccount.getAmount().compareTo(value) == -1) {
                    throw new NotEnoughFundsException(sAccount, value);
                }
                sAccount.subtract(value);
                tAccount.add(value);
                TransferDetails details = new TransferDetails(sAccount, tAccount, value);
                return details;
            }
        }
    }

    private static String checkAndTrimId(String id) {
        if (id == null || (id = id.trim()).isEmpty()) {
            throw new AccountException(StatusCode.INVALID_ID,
                    String.format("Invalid account id '%s'.",id));
        }
        return id;
    }

    private static void checkSameId(String id, String targetId) {
        if (Objects.equals(id, targetId)) {
            throw new AccountException(StatusCode.SAME_ACCOUNT_TRANSFER,
                   "Source and target ids equal");
        }
    }

    private static void checkInitValue(BigDecimal initValue) {
        if (initValue == null || BigDecimal.ZERO.compareTo(initValue) == 1) {
            throw new AccountException(StatusCode.INVALID_INIT_VALUE,
                "Invalid account init value "+(initValue == null ? "null" : initValue.toPlainString())+".");
        }
    }

    private static void checkTransferValue(BigDecimal transferValue) {
        if (transferValue == null || BigDecimal.ZERO.compareTo(transferValue) >= 0) {
            throw new AccountException(StatusCode.INVALID_TRANSFER_VALUE, 
                "Invalid transfer value "+(transferValue == null ? "null" : transferValue.toPlainString())+".");
        }
    }
}
