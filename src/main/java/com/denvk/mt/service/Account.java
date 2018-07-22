package com.denvk.mt.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Denis Voroshchuk
 */
public class Account {
    
    @JsonIgnore
    private final long internalID;
    @JsonIgnore
    private static final AtomicLong ORDER_GEN = new AtomicLong(0);
    @JsonProperty
    private final String id;//user id.
    @JsonProperty
    private BigDecimal amount;

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void add(BigDecimal value) {
        this.amount = this.amount.add(value);
    }
    
    public void subtract(BigDecimal value) {
        this.amount = this.amount.subtract(value);
    }
    
    public static Account create(String id,BigDecimal amount) {
        return new Account(id, amount, ORDER_GEN.getAndIncrement());
    }
    
    public static Account[] order(Account sAccount, Account tAccount) {
        Account[] ordered = new Account[2];
        ordered[0] = sAccount.internalID < tAccount.internalID ? sAccount : tAccount;
        ordered[1] = sAccount.internalID < tAccount.internalID ? tAccount : sAccount;
        return ordered;
    }

    private Account(String id,BigDecimal amount,long internalID) {
        this.internalID = internalID;
        this.id = id;
        this.amount = amount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (int) (this.internalID ^ (this.internalID >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Account other = (Account) obj;
        return this.internalID == other.internalID;
    }

    @Override
    public String toString() {
        return "Account{" + "id=" + id + ", amount=" + amount + '}';
    }
}
