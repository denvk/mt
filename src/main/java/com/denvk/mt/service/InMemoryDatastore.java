package com.denvk.mt.service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author Denis Voroshchuk
 */
public class InMemoryDatastore {

    private final ConcurrentHashMap<String, Account> data = new ConcurrentHashMap();

    public Account create(String key, BigDecimal value) {
        Factory fct = new Factory(value);
        Account current = data.computeIfAbsent(key, fct);
        return fct.isCreated() ? current : null;
    }

    public Account get(String key) {
        return data.get(key);
    }

    private static class Factory implements Function<String, Account> {

        public boolean created = false;
        private final BigDecimal value;

        public Factory(BigDecimal value) {
            this.value = value;
        }

        @Override
        public Account apply(String t) {
            created = true;
            return Account.create(t, value);
        }

        public boolean isCreated() {
            return created;
        }
    }
}
