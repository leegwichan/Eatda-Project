package eatda.repository.store;

import eatda.domain.store.Store;

public interface StorePopularity {

    long getStoreId();

    long getCheerCount();

    default boolean isMatchStoreId(Store store) {
        return getStoreId() == store.getId();
    }
}
