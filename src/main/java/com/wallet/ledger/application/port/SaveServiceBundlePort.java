package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.ServiceBundle;

import java.util.UUID;

public interface SaveServiceBundlePort {

    /**
     * Create a new service bundle under a provisioning service.
     */
    ServiceBundle create(ServiceBundle bundle);

    /**
     * Update an existing service bundle by id.
     */
    ServiceBundle update(UUID id, ServiceBundle bundle);
}
