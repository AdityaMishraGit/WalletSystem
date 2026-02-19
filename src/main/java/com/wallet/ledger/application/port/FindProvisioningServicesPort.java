package com.wallet.ledger.application.port;

import com.wallet.ledger.domain.entity.ProvisioningService;
import com.wallet.ledger.domain.valueobject.ProvisioningServiceType;

import java.util.List;
import java.util.Optional;

public interface FindProvisioningServicesPort {

    List<ProvisioningService> findAll(String serviceType, String subcategory);

    Optional<ProvisioningService> findById(java.util.UUID id);
}
