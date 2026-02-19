package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.ProvisioningService;

import java.util.UUID;

public interface SaveProvisioningServicePort {

    /**
     * Create a new provisioning service. Id and createdAt are set by the adapter if not set.
     */
    ProvisioningService create(ProvisioningService service);

    /**
     * Update an existing provisioning service by id. Returns the updated entity. Throws if not found.
     */
    ProvisioningService update(UUID id, ProvisioningService service);
}
