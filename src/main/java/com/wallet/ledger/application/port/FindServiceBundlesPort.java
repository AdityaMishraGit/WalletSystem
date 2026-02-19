package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.ServiceBundle;
import com.wallet.ledger.domain.valueobject.AmountFilter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindServiceBundlesPort {

    /**
     * Find bundles with optional filters. Bundles have a fixed amount; when amount and amountFilter
     * are provided, returns bundles whose fixed_amount is greater than or less than the given amount.
     */
    List<ServiceBundle> findBundles(String serviceType, BigDecimal amount, AmountFilter amountFilter,
                                    String subcategory, UUID serviceId);

    Optional<ServiceBundle> findBundleById(UUID bundleId);
}
